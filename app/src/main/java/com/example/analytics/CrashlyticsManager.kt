package com.example.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashlyticsManager(private val crashlytics: FirebaseCrashlytics) {

    private val _totalExceptions = MutableStateFlow(0)
    val totalExceptions = _totalExceptions.asStateFlow()

    private val _lastException = MutableStateFlow<String?>(null)
    val lastException = _lastException.asStateFlow()

    init {
        // Initialize flags or base configuration if needed.
        // We ensure crash reporting is active. Usually, it's automatic.
        crashlytics.setCrashlyticsCollectionEnabled(true)
    }

    fun setCustomKeys(
        currentPlan: String,
        goalType: String,
        activityLevel: String,
        authProvider: String,
        appVersion: String
    ) {
        crashlytics.setCustomKey("current_plan", currentPlan)
        crashlytics.setCustomKey("goal_type", goalType)
        crashlytics.setCustomKey("activity_level", activityLevel)
        crashlytics.setCustomKey("auth_provider", authProvider)
        crashlytics.setCustomKey("app_version", appVersion)
    }

    fun recordException(e: Throwable, contextMessage: String) {
        val wrappedException = Exception("$contextMessage: ${e.message}", e)
        crashlytics.recordException(wrappedException)
        
        _totalExceptions.value += 1
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _lastException.value = "[$timestamp] $contextMessage: ${e.message}"
    }

    // Helper for direct string-based error scenarios
    fun recordExceptionMessage(contextMessage: String) {
        val e = Exception(contextMessage)
        crashlytics.recordException(e)

        _totalExceptions.value += 1
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _lastException.value = "[$timestamp] $contextMessage"
    }
}
