package com.example.petrunning2.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class RunReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = prefs.getBoolean(KEY_ENABLED, false)

        when (intent.action) {
            // 재부팅 후 알람 재등록
            Intent.ACTION_BOOT_COMPLETED -> {
                if (enabled) {
                    NotificationHelper.scheduleRepeating(
                        context,
                        prefs.getInt(KEY_HOUR, 9),
                        prefs.getInt(KEY_INTERVAL_DAYS, 1),
                    )
                }
            }
            // 알람 트리거 → 알림 표시
            else -> {
                if (enabled) {
                    val petName = prefs.getString(KEY_PET_NAME, "Runi") ?: "Runi"
                    NotificationHelper.showReminder(context, petName)
                }
            }
        }
    }

    companion object {
        const val PREFS_NAME = "notification_prefs"
        const val KEY_ENABLED = "enabled"
        const val KEY_HOUR = "hour"
        const val KEY_INTERVAL_DAYS = "interval_days"
        const val KEY_PET_NAME = "pet_name"
    }
}
