package com.example.petrunning2.ui.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.analytics.AnalyticsHelper
import com.example.petrunning2.data.repository.DogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val routePoints: String = "", // "lat,lng|lat,lng|..."
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val dogRepository: DogRepository,
    private val analyticsHelper: AnalyticsHelper,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private var hasSaved = false

    fun setRunData(distanceKm: Double, elapsedSeconds: Long, paceSecPerKm: Long, routePoints: String = "") {
        val xp = (distanceKm * 10).toInt().coerceAtLeast(if (distanceKm > 0.0) 1 else 0)
        val credit = (distanceKm * 20).toInt()
        val calories = (distanceKm * 60 + elapsedSeconds * 0.05).toInt()
        // 결과 화면에서는 전체 평균 페이스 (총 시간 ÷ 총 거리)
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
            _uiState.value = _uiState.value.copy(isSaving = false, saved = true)
            onComplete()
        }
    }

    fun logResultShared() {
        analyticsHelper.logResultShared()
    }
}
