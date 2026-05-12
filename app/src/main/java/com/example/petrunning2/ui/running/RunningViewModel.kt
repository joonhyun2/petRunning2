package com.example.petrunning2.ui.running

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.analytics.AnalyticsHelper
import com.example.petrunning2.data.location.LocationDataSource
import com.example.petrunning2.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LatLngPoint(val lat: Double, val lng: Double)

data class FloatingReward(val id: Long, val text: String, val isCredit: Boolean)

@HiltViewModel
class RunningViewModel @Inject constructor(
    private val locationDataSource: LocationDataSource,
    private val analyticsHelper: AnalyticsHelper,
    itemRepository: ItemRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RunningUiState())
    val uiState: StateFlow<RunningUiState> = _uiState.asStateFlow()

    // 경로 좌표 저장
    private val _routePoints = MutableStateFlow<List<LatLngPoint>>(emptyList())
    val routePoints: StateFlow<List<LatLngPoint>> = _routePoints.asStateFlow()

    // 0.01km마다 캐릭터 머리 위에 띄울 보상 이벤트
    private val _rewardEvents = MutableSharedFlow<FloatingReward>(extraBufferCapacity = 32)
    val rewardEvents: SharedFlow<FloatingReward> = _rewardEvents.asSharedFlow()

    // 장착 아이템
    val equippedItemId: StateFlow<Int?> = itemRepository.equippedItemId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    private var timerJob: Job? = null
    private var locationJob: Job? = null

    private var lastLocation: Location? = null
    private var milestoneHundredths = 0  // 마지막으로 알린 0.01km 단위 마일스톤
    private var rewardIdCounter = 0L

    fun startRun() {
        if (_uiState.value.status == RunStatus.RUNNING) return
        _uiState.update { it.copy(status = RunStatus.RUNNING) }
        analyticsHelper.logRunStarted()
        startTimer()
        startLocationTracking()
    }

    fun pauseRun() {
        if (_uiState.value.status != RunStatus.RUNNING) return
        timerJob?.cancel()
        locationJob?.cancel()
        lastLocation = null
        val state = _uiState.value
        analyticsHelper.logRunPaused(state.elapsedSeconds, state.distanceKm)
        _uiState.update { it.copy(status = RunStatus.PAUSED) }
    }

    fun resumeRun() {
        if (_uiState.value.status != RunStatus.PAUSED) return
        _uiState.update { it.copy(status = RunStatus.RUNNING) }
        analyticsHelper.logRunStarted() // resume도 run_started로 통합
        startTimer()
        startLocationTracking()
    }

    fun stopRun() {
        timerJob?.cancel()
        locationJob?.cancel()
        lastLocation = null
        milestoneHundredths = 0
        _uiState.update { it.copy(status = RunStatus.IDLE) }
    }

    fun logRunCancelled(elapsedSeconds: Long, distanceKm: Double, reason: String) {
        analyticsHelper.logRunCancelled(elapsedSeconds, distanceKm, reason)
    }

    fun getRouteSnapshot(): List<LatLngPoint> = _routePoints.value

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                // 페이스는 GPS 핸들러에서만 갱신 — 타이머는 시간만 증가
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun startLocationTracking() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationDataSource.locationFlow()
                .catch { /* GPS 오류 시 조용히 무시 */ }
                .collect { location ->
                    // 정확도 20m 초과 수신은 무시 (GPS 초기화 시 튀는 좌표 필터)
                    if (location.accuracy > 20f) return@collect

                    _routePoints.update { it + LatLngPoint(location.latitude, location.longitude) }

                    val prev = lastLocation
                    if (prev != null) {
                        val deltaMeters = prev.distanceTo(location)
                        val deltaMs = (location.elapsedRealtimeNanos - prev.elapsedRealtimeNanos) / 1_000_000L
                        // 사람 최대 속도(10 m/s ≈ 36 km/h) 초과면 GPS 노이즈로 버림
                        val speedMs = if (deltaMs > 0) deltaMeters / (deltaMs / 1000.0) else 0.0
                        if (deltaMeters >= 5f && speedMs <= 10.0) {
                            _uiState.update { current ->
                                val newDistKm = current.distanceKm + (deltaMeters / 1000.0)
                                // 순간 페이스: 이번 GPS 구간의 속도로 계산 (초/km)
                                // speedMs = m/s → 1000/speedMs = 초/km
                                val newPace = if (speedMs > 0.1)
                                    (1000.0 / speedMs).toLong()
                                else current.paceSecPerKm  // 속도 너무 낮으면 이전 값 유지
                                // 0.01km(10m)마다 보상 이벤트 발생
                                val newHundredths = (newDistKm * 100).toInt()
                                if (newHundredths > milestoneHundredths) {
                                    repeat(newHundredths - milestoneHundredths) { i ->
                                        val mNum = milestoneHundredths + i + 1
                                        // 5번째(0.05km)마다 크레딧, 나머지는 경험치
                                        val isCredit = mNum % 5 == 0
                                        _rewardEvents.tryEmit(
                                            FloatingReward(
                                                id = ++rewardIdCounter,
                                                text = if (isCredit) "+1 크레딧" else "+1 경험치",
                                                isCredit = isCredit,
                                            )
                                        )
                                    }
                                    milestoneHundredths = newHundredths
                                }
                                current.copy(distanceKm = newDistKm, paceSecPerKm = newPace)
                            }
                        }
                    }
                    lastLocation = location
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        locationJob?.cancel()
    }
}
