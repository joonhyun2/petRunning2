package com.example.petrunning2.ui.statics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.analytics.AnalyticsHelper
import com.example.petrunning2.data.Dog
import com.example.petrunning2.data.local.entity.RunRecordEntity
import com.example.petrunning2.data.repository.DogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    dogRepository: DogRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    val dog: StateFlow<Dog> = dogRepository.dog
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Dog(name = "Pix", level = 1, currentXp = 0, maxXp = 100, credit = 0)
        )

    val records: StateFlow<List<RunRecordEntity>> = dogRepository.getAllRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun logRecentActivityClicked() {
        analyticsHelper.logRecentActivityClicked()
    }
}
