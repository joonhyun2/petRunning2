package com.example.petrunning2.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.data.Dog
import com.example.petrunning2.data.repository.DogRepository
import com.example.petrunning2.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TodayStats(
    val totalTimeSeconds: Long = 0L,
    val totalDistanceKm: Double = 0.0,
    val avgPaceSecPerKm: Long = 0L
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    dogRepository: DogRepository,
    itemRepository: ItemRepository,
) : ViewModel() {

    val dog: StateFlow<Dog> = dogRepository.dog
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Dog(name = "Pix", level = 1, currentXp = 0, maxXp = 100, credit = 0)
        )

    val equippedItemId: StateFlow<Int?> = itemRepository.equippedItemId
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val equippedItemIds: StateFlow<List<Int>> = itemRepository.equippedItemIds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val todayStats: StateFlow<TodayStats> = dogRepository.getTodayRecords()
        .map { records ->
            if (records.isEmpty()) {
                TodayStats()
            } else {
                val totalTime = records.sumOf { it.elapsedSeconds }
                val totalDistance = records.sumOf { it.distanceKm }
                val avgPace = if (totalDistance > 0) {
                    (totalTime / totalDistance).toLong()
                } else 0L
                TodayStats(
                    totalTimeSeconds = totalTime,
                    totalDistanceKm = totalDistance,
                    avgPaceSecPerKm = avgPace
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TodayStats()
        )
}
