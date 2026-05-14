package com.example.petrunning2.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.data.Dog
import com.example.petrunning2.analytics.AnalyticsHelper
import com.example.petrunning2.data.repository.CatalogRepository
import com.example.petrunning2.data.repository.DogRepository
import com.example.petrunning2.data.repository.ItemRepository
import com.example.petrunning2.ui.decoration.CLOTHES_CATALOG
import com.example.petrunning2.ui.decoration.ClothItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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
    private val catalogRepository: CatalogRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    fun logScreenView() = analyticsHelper.logScreenView("home")
    fun logStartButtonTapped() = analyticsHelper.logHomeStartButtonTapped()
    fun logTabDwellTime(seconds: Long) = analyticsHelper.logTabDwellTime("home", seconds)

    private val localItemIds = CLOTHES_CATALOG.map { it.id }.toSet()

    init {
        viewModelScope.launch { catalogRepository.fetchCharacterSpriteUrl() }
    }

    val characterSpriteUrl: StateFlow<String?> = catalogRepository.characterSpriteUrl
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null,
        )

    val dog: StateFlow<Dog> = dogRepository.dog
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Dog(name = "Runi", level = 1, currentXp = 0, maxXp = 100, credit = 0)
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
