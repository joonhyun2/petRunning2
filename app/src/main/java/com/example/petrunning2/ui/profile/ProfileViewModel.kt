package com.example.petrunning2.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petrunning2.data.Dog
import com.example.petrunning2.data.local.entity.DogEntity
import com.example.petrunning2.data.repository.DogRepository
import com.example.petrunning2.notification.NotificationHelper
import com.example.petrunning2.notification.RunReminderReceiver.Companion.KEY_ENABLED
import com.example.petrunning2.notification.RunReminderReceiver.Companion.KEY_PET_NAME
import com.example.petrunning2.notification.RunReminderReceiver.Companion.PREFS_NAME
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val dogRepository: DogRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val dog: StateFlow<Dog> = dogRepository.dog
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DogEntity().let {
                Dog(name = it.name, level = it.level, currentXp = it.currentXp, maxXp = it.maxXp, credit = it.credit)
            },
        )

    private val _notificationEnabled = MutableStateFlow(prefs.getBoolean(KEY_ENABLED, false))
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled

    fun updateName(name: String) {
        viewModelScope.launch {
            dogRepository.updateName(name)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        // 캐릭터 이름도 함께 저장 (알림 메시지에 사용)
        val petName = dog.value.name
        prefs.edit()
            .putBoolean(KEY_ENABLED, enabled)
            .putString(KEY_PET_NAME, petName)
            .apply()
        _notificationEnabled.value = enabled
        if (enabled) {
            NotificationHelper.scheduleRepeating(context, hour = 9, intervalDays = 2)
        } else {
            NotificationHelper.cancel(context)
        }
    }
}
