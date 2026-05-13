package com.example.petrunning2.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticsHelper @Inject constructor() {

    private val analytics: FirebaseAnalytics = Firebase.analytics

    // ── 앱 라이프사이클 ──────────────────────────────────────────────────────

    fun logScreenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        })
    }

    // ── 러닝 Funnel ──────────────────────────────────────────────────────────

    fun logRunStarted() {
        analytics.logEvent("run_started", null)
    }

    fun logRunPaused(elapsedSeconds: Long, distanceKm: Double) {
        analytics.logEvent("run_paused", Bundle().apply {
            putLong("elapsed_seconds", elapsedSeconds)
            putDouble("distance_km", distanceKm)
        })
    }

    fun logRunResumed() {
        analytics.logEvent("run_resumed", null)
    }

    fun logRunCompleted(distanceKm: Double, elapsedSeconds: Long, avgPaceSecPerKm: Long) {
        analytics.logEvent("run_completed", Bundle().apply {
            putDouble("distance_km", distanceKm)
            putLong("elapsed_seconds", elapsedSeconds)
            putLong("avg_pace_sec_per_km", avgPaceSecPerKm)
            putLong("coins_earned", (distanceKm * 10).toLong())
        })
    }

    fun logRunCancelled(elapsedSeconds: Long, distanceKm: Double, reason: String) {
        analytics.logEvent("run_cancelled", Bundle().apply {
            putLong("elapsed_seconds", elapsedSeconds)
            putDouble("distance_km", distanceKm)
            putString("reason", reason) // "too_short", "user_quit"
        })
    }

    fun logResultSaved(distanceKm: Double, elapsedSeconds: Long) {
        analytics.logEvent("result_saved", Bundle().apply {
            putDouble("distance_km", distanceKm)
            putLong("elapsed_seconds", elapsedSeconds)
        })
    }

    fun logResultShared() {
        analytics.logEvent("result_shared", null)
    }

    // ── 꾸미기 Funnel ────────────────────────────────────────────────────────

    fun logDecorationTabOpened(source: String, coinsBalance: Int) {
        analytics.logEvent("decoration_tab_opened", Bundle().apply {
            putString("source", source) // "bottom_nav", "after_run"
            putLong("coins_balance", coinsBalance.toLong())
        })
    }

    fun logDecorationCategoryViewed(category: String) {
        analytics.logEvent("decoration_category_viewed", Bundle().apply {
            putString("category", category) // "face", "hair", "cloth", "theme"
        })
    }

    fun logItemViewed(itemId: Int, itemName: String, isLocked: Boolean) {
        analytics.logEvent("item_viewed", Bundle().apply {
            putLong("item_id", itemId.toLong())
            putString("item_name", itemName)
            putBoolean("is_locked", isLocked)
        })
    }

    fun logPurchaseDialogOpened(itemId: Int, itemName: String, price: Int, coinsBalance: Int) {
        analytics.logEvent("purchase_dialog_opened", Bundle().apply {
            putLong("item_id", itemId.toLong())
            putString("item_name", itemName)
            putLong("price", price.toLong())
            putLong("coins_balance", coinsBalance.toLong())
        })
    }

    fun logItemPurchased(itemId: Int, itemName: String, price: Int, category: String) {
        analytics.logEvent("item_purchased", Bundle().apply {
            putLong("item_id", itemId.toLong())
            putString("item_name", itemName)
            putLong("price", price.toLong())
            putString("category", category)
        })
    }

    fun logItemEquipped(itemId: Int, itemName: String, category: String) {
        analytics.logEvent("item_equipped", Bundle().apply {
            putLong("item_id", itemId.toLong())
            putString("item_name", itemName)
            putString("category", category)
        })
    }

    fun logItemUnequipped(category: String) {
        analytics.logEvent("item_unequipped", Bundle().apply {
            putString("category", category)
        })
    }

    fun logDecorationReset() {
        analytics.logEvent("decoration_reset", null)
    }

    fun logEvent(eventName: String, itemId: Int, itemName: String) {
        analytics.logEvent(eventName, Bundle().apply {
            putLong("item_id", itemId.toLong())
            putString("item_name", itemName)
        })
    }

    fun logHomeStartButtonTapped() {
        analytics.logEvent("home_start_button_tapped", null)
    }

    fun logTabDwellTime(tabName: String, durationSeconds: Long) {
        analytics.logEvent("tab_dwell_time", Bundle().apply {
            putString("tab_name", tabName)
            putLong("duration_seconds", durationSeconds)
        })
    }

    // ── 탭 네비게이션 Feature Usage ──────────────────────────────────────────

    fun logTabClicked(tabName: String) {
        analytics.logEvent("tab_clicked", Bundle().apply {
            putString("tab_name", tabName) // "home", "decoration", "stats", "profile"
        })
    }

    // ── 통계 Feature Usage ───────────────────────────────────────────────────

    fun logStatsPeriodChanged(period: String) {
        analytics.logEvent("stats_period_changed", Bundle().apply {
            putString("period", period) // "weekly", "monthly", "yearly"
        })
    }

    fun logRecentActivityClicked() {
        analytics.logEvent("recent_activity_clicked", null)
    }

    // ── 유저 속성 (Retention 코호트용) ───────────────────────────────────────

    fun setUserProperties(totalRuns: Int, totalDistanceKm: Double, petLevel: Int) {
        analytics.setUserProperty("total_run_count", totalRuns.toString())
        analytics.setUserProperty("total_distance_km", "%.1f".format(totalDistanceKm))
        analytics.setUserProperty("pet_level", petLevel.toString())
    }
}
