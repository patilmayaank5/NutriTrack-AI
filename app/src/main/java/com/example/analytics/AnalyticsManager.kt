package com.example.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsManager(private val firebaseAnalytics: FirebaseAnalytics) {

    // For Debug Screen
    private val _eventLogs = MutableStateFlow<List<String>>(emptyList())
    val eventLogs = _eventLogs.asStateFlow()
    
    private val _totalEvents = MutableStateFlow(0)
    val totalEvents = _totalEvents.asStateFlow()

    var onAnalyticsError: ((Exception) -> Unit)? = null

    private fun logEventInternal(eventName: String, params: Bundle? = null) {
        try {
            firebaseAnalytics.logEvent(eventName, params)
            
            // Debug
            _totalEvents.value += 1
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val logLine = "$timestamp - $eventName ${params?.let { formatBundle(it) } ?: "[]"}"
            val currentLogs = _eventLogs.value.toMutableList()
            currentLogs.add(0, logLine)
            if (currentLogs.size > 20) {
                currentLogs.removeAt(currentLogs.size - 1)
            }
            _eventLogs.value = currentLogs
        } catch (e: Exception) {
            onAnalyticsError?.invoke(e)
        }
    }

    private fun formatBundle(bundle: Bundle): String {
        return bundle.keySet().joinToString(prefix = "[", postfix = "]") { "$it=${bundle.get(it)}" }
    }

    private fun bundleOf(vararg pairs: Pair<String, Any?>): Bundle {
        val bundle = Bundle()
        for ((key, value) in pairs) {
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Float -> bundle.putFloat(key, value)
                is Double -> bundle.putDouble(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                is Long -> bundle.putLong(key, value)
            }
        }
        return bundle
    }

    fun logPremiumGateShown(featureName: String) {
        logEventInternal("premium_gate_shown", bundleOf("feature_name" to featureName))
    }

    fun logPremiumUpgradeClicked(featureName: String) {
        logEventInternal("premium_upgrade_clicked", bundleOf("feature_name" to featureName))
    }

    fun logPremiumFeatureBlocked(featureName: String) {
        logEventInternal("premium_feature_blocked", bundleOf("feature_name" to featureName))
    }

    fun logSignupCompleted(authProvider: String) {
        logEventInternal("signup_completed", bundleOf("auth_provider" to authProvider))
    }

    fun logEmailVerified() {
        logEventInternal("email_verified")
    }

    fun logLoginSuccess() {
        logEventInternal("login_success", bundleOf("auth_provider" to "password"))
    }

    fun logGoogleLoginSuccess() {
        logEventInternal("google_login_success", bundleOf("auth_provider" to "google"))
    }

    fun logPasswordResetRequested() {
        logEventInternal("password_reset_requested")
    }

    fun logLogout() {
        logEventInternal("logout")
    }

    fun logProfileSetupCompleted(goalType: String, activityLevel: String) {
        logEventInternal("profile_setup_completed", bundleOf(
            "goal_type" to goalType,
            "activity_level" to activityLevel
        ))
    }

    fun logAppTourCompleted() {
        logEventInternal("app_tour_completed")
    }

    fun logPremiumIntroViewed() {
        logEventInternal("premium_intro_viewed")
    }

    fun logContinueFreeClicked() {
        logEventInternal("continue_free_clicked")
    }

    fun logUpgradeProClicked() {
        logEventInternal("upgrade_pro_clicked")
    }

    fun logMealLogged(mealType: String) {
        logEventInternal("meal_logged", bundleOf("meal_type" to mealType))
    }

    fun logMealDeleted() {
        logEventInternal("meal_deleted")
    }

    fun logMealScanned() {
        logEventInternal("meal_scanned")
    }

    fun logMealScanEdited() {
        logEventInternal("meal_scan_edited")
    }

    fun logMealPresetSaved() {
        logEventInternal("meal_preset_saved")
    }

    fun logMealPresetUsed() {
        logEventInternal("meal_preset_used")
    }

    fun logWaterLogged() {
        logEventInternal("water_logged")
    }

    fun logWeightLogged() {
        logEventInternal("weight_logged")
    }

    fun logFitnessTipViewed() {
        logEventInternal("fitness_tip_viewed")
    }

    fun logWeeklyReportGenerated() {
        logEventInternal("weekly_report_generated")
    }

    fun logCoachMessageSent() {
        logEventInternal("coach_message_sent")
    }

    fun logAiCoachMessageSent(currentPlan: String, messagesUsed: Int) {
        logEventInternal("ai_coach_message_sent", bundleOf("current_plan" to currentPlan, "messages_used" to messagesUsed))
    }

    fun logAiCoachLimitReached(currentPlan: String, messagesUsed: Int) {
        logEventInternal("ai_coach_limit_reached", bundleOf("current_plan" to currentPlan, "messages_used" to messagesUsed))
    }

    fun logProfileUpdated() {
        logEventInternal("profile_updated")
    }

    fun logGoalChanged(goalType: String) {
        logEventInternal("goal_changed", bundleOf("goal_type" to goalType))
    }

    fun logAccountDeleted(currentPlan: String) {
        logEventInternal("account_deleted", bundleOf("current_plan" to currentPlan))
    }
}
