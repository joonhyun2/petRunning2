package com.example.petrunning2.ui.result

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.analytics.AnalyticsHelper
import com.example.petrunning2.data.repository.DogRepository
import com.example.petrunning2.util.LocationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResultUiState(
    val distanceKm: Double = 0.0,
    val elapsedSeconds: Long = 0L,
    val paceSecPerKm: Long = 0L,
    val calories: Int = 0,
    val earnedXp: Int = 0,
    val earnedCredit: Int = 0,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val routePoints: String = "",
    val locationLabel: String = "",
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val dogRepository: DogRepository,
    private val analyticsHelper: AnalyticsHelper,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private var hasSaved = false

    fun setRunData(distanceKm: Double, elapsedSeconds: Long, paceSecPerKm: Long, routePoints: String = "") {
        val xp = (distanceKm * 25).toInt().coerceAtLeast(if (distanceKm > 0.0) 1 else 0)
        val credit = (distanceKm * 10).toInt()
        val calories = (distanceKm * 60 + elapsedSeconds * 0.05).toInt()
        val avgPace = if (distanceKm > 0.0) (elapsedSeconds / distanceKm).toLong() else paceSecPerKm
        _uiState.value = ResultUiState(
            distanceKm = distanceKm,
            elapsedSeconds = elapsedSeconds,
            paceSecPerKm = avgPace,
            calories = calories,
            earnedXp = xp,
            earnedCredit = credit,
            routePoints = routePoints,
        )
        // 첫 번째 좌표로 위치 라벨 역지오코딩
        val firstPoint = LocationUtils.parseFirstPoint(routePoints)
        if (firstPoint != null) {
            viewModelScope.launch {
                val label = LocationUtils.resolveLocationLabel(context, firstPoint.first, firstPoint.second)
                _uiState.value = _uiState.value.copy(locationLabel = label)
            }
        }
    }

    fun saveResult(onComplete: () -> Unit) {
        if (hasSaved) {
            onComplete()
            return
        }
        hasSaved = true
        _uiState.value = _uiState.value.copy(isSaving = true)
        viewModelScope.launch {
            val state = _uiState.value
            dogRepository.addXp(state.earnedXp)
            dogRepository.addCredit(state.earnedCredit)
            dogRepository.saveRunRecord(
                distanceKm = state.distanceKm,
                elapsedSeconds = state.elapsedSeconds,
                paceSecPerKm = state.paceSecPerKm,
                xpGained = state.earnedXp,
                routePoints = state.routePoints,
            )
            analyticsHelper.logRunCompleted(
                distanceKm = state.distanceKm,
                elapsedSeconds = state.elapsedSeconds,
                avgPaceSecPerKm = state.paceSecPerKm,
            )
            analyticsHelper.logResultSaved(state.distanceKm, state.elapsedSeconds)

            // Retention 코호트용 user property 업데이트
            val allRecords = dogRepository.getAllRecords().first()
            val updatedDog = dogRepository.dog.first()
            analyticsHelper.setUserProperties(
                totalRuns = allRecords.size,
                totalDistanceKm = allRecords.sumOf { it.distanceKm },
                petLevel = updatedDog.level,
            )

            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
            onComplete()
        }
    }

    fun logResultShared() {
        analyticsHelper.logResultShared()
    }
}
