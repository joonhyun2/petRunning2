package com.example.petrunning2.ui.running

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.R
import com.example.petrunning2.analytics.AnalyticsHelper
import com.example.petrunning2.data.location.LocationDataSource
import com.example.petrunning2.data.repository.CatalogRepository
import com.example.petrunning2.data.repository.ItemRepository
import com.example.petrunning2.ui.decoration.CLOTHES_CATALOG
import com.example.petrunning2.ui.decoration.ClothItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import kotlinx.coroutines.flow.map
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
    @ApplicationContext private val context: Context,
    itemRepository: ItemRepository,
    catalogRepository: CatalogRepository,
) : ViewModel() {

    private val localItemIds = CLOTHES_CATALOG.map { it.id }.toSet()

    val catalog: StateFlow<List<ClothItem>> = catalogRepository.items
        .map { dbItems ->
            val newItems = dbItems.filter { it.id !in localItemIds }
            CLOTHES_CATALOG + newItems
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CLOTHES_CATALOG,
        )

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
    private var milestoneHundredths = 0
    private var rewardIdCounter = 0L

    // 페이스 롤링 평균: 최근 30초 구간의 (시각ms, 누적거리km) 버퍼
    private data class PacePoint(val timeMs: Long, val totalDistKm: Double)
    private val paceBuffer = ArrayDeque<PacePoint>()
    private var lastPaceDisplayMs = 0L

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
        paceBuffer.clear()
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
        paceBuffer.clear()
        lastPaceDisplayMs = 0L
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
                    // 정확도 25m 초과 수신은 무시 (GPS 초기화 시 튀는 좌표 필터)
                    if (location.accuracy > 25f) return@collect

                    val prev = lastLocation
                    val speedMs: Double
                    val deltaMeters: Float
                    if (prev != null) {
                        deltaMeters = prev.distanceTo(location)
                        val deltaMs = (location.elapsedRealtimeNanos - prev.elapsedRealtimeNanos) / 1_000_000L
                        speedMs = if (deltaMs > 0) deltaMeters / (deltaMs / 1000.0) else 0.0
                        if (speedMs > 10.0) {
                            // GPS 노이즈 점프: 경로에도 찍지 않고 기준점도 갱신하지 않음
                            return@collect
                        }
                    } else {
                        deltaMeters = 0f
                        speedMs = 0.0
                    }

                    // 속도 검증 통과한 좌표만 지도 경로에 추가
                    _routePoints.update { it + LatLngPoint(location.latitude, location.longitude) }

                    if (prev != null && speedMs >= 0.1) {
                        val nowMs = System.currentTimeMillis()
                        val newDistKm = _uiState.value.distanceKm + (deltaMeters / 1000.0)

                        // 페이스 롤링 버퍼 업데이트
                        paceBuffer.addLast(PacePoint(nowMs, newDistKm))
                        while (paceBuffer.size > 1 && nowMs - paceBuffer.first().timeMs > PACE_WINDOW_MS) {
                            paceBuffer.removeFirst()
                        }

                        // 30초 롤링 평균 페이스 계산
                        val rollingPace: Long? = if (paceBuffer.size >= 2) {
                            val oldest = paceBuffer.first()
                            val timeSec = (nowMs - oldest.timeMs) / 1000.0
                            val distKm = newDistKm - oldest.totalDistKm
                            if (distKm > 0.001) (timeSec / distKm).toLong() else null
                        } else null

                        // 10초마다 화면 페이스 갱신
                        val shouldUpdatePace = rollingPace != null &&
                            nowMs - lastPaceDisplayMs >= PACE_DISPLAY_INTERVAL_MS
                        if (shouldUpdatePace) lastPaceDisplayMs = nowMs

                        _uiState.update { current ->
                            val newHundredths = (newDistKm * 100).toInt()
                            if (newHundredths > milestoneHundredths) {
                                repeat(newHundredths - milestoneHundredths) { i ->
                                    val mNum = milestoneHundredths + i + 1
                                    val isCredit = mNum % 5 == 0
                                    _rewardEvents.tryEmit(
                                        FloatingReward(
                                            id = ++rewardIdCounter,
                                            text = if (isCredit) context.getString(R.string.running_reward_credit) else context.getString(R.string.running_reward_xp),
                                            isCredit = isCredit,
                                        )
                                    )
                                }
                                milestoneHundredths = newHundredths
                            }
                            current.copy(
                                distanceKm = newDistKm,
                                paceSecPerKm = if (shouldUpdatePace) rollingPace!! else current.paceSecPerKm,
                            )
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

    companion object {
        private const val PACE_WINDOW_MS = 30_000L         // 30초 롤링 평균 창
        private const val PACE_DISPLAY_INTERVAL_MS = 7_000L  // 7초마다 화면 갱신
    }
}
