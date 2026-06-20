package com.example.ui

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.example.analytics.AnalyticsManager
import com.example.analytics.CrashlyticsManager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.*
import com.example.data.repository.NutriRepository
import com.example.data.api.DetectedFood
import com.example.data.model.FoodCatalog
import com.example.data.model.FoodItem
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object VerifyEmail : Screen()
    object ForgotPassword : Screen()
    object SetupGoals : Screen()
    object AppTour : Screen()
    object PremiumIntro : Screen()
    object Dashboard : Screen()
    object SearchAdd : Screen()
    object AiQuickAdd : Screen()
    object Analytics : Screen()
    object Chatbot : Screen()
    object Profile : Screen()
    object AnalyticsDebug : Screen()
}

class NutriViewModel(application: Application) : AndroidViewModel(application) {

    val analyticsManager = AnalyticsManager(FirebaseAnalytics.getInstance(application))
    val crashlyticsManager = CrashlyticsManager(FirebaseCrashlytics.getInstance())

    private val database: com.example.data.database.NutriDatabase by lazy {
        try {
            com.example.data.database.NutriDatabase.getDatabase(application)
        } catch (e: Exception) {
            crashlyticsManager.recordException(e, "Room migration failure")
            application.deleteDatabase("nutritrack_database")
            com.example.data.database.NutriDatabase.getDatabase(application)
        }
    }
    
    private val repository: com.example.data.repository.NutriRepository by lazy {
        com.example.data.repository.NutriRepository(database.nutriDao())
    }

    // --- State Management ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true) // Premium dark by default
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    private val appTourCompletedKey = booleanPreferencesKey("app_tour_completed")
    private val premiumSeenKey = booleanPreferencesKey("premium_seen")
    private val darkModeKey = booleanPreferencesKey("dark_mode")
    private val reminderNotificationsKey = booleanPreferencesKey("reminders_enabled")
    private val waterReminderKey = booleanPreferencesKey("water_reminders")
    private val weightReminderKey = booleanPreferencesKey("weight_reminders")

    private val isPremiumKey = booleanPreferencesKey("is_premium_pro")
    private val monthlyMealScansKey = intPreferencesKey("monthly_meal_scans")
    private val monthlyMealScansMonthStrKey = stringPreferencesKey("monthly_meal_scans_ref_month")
    private val weeklyReportsRunKey = intPreferencesKey("weekly_reports_run")
    private val weeklyReportsWeekStrKey = stringPreferencesKey("weekly_reports_ref_week")

    private val dailyAiCoachMessagesKey = intPreferencesKey("daily_ai_coach_messages_used")
    private val dailyAiCoachResetDateKey = stringPreferencesKey("last_ai_coach_reset_date")

    private val _isAppTourCompleted = MutableStateFlow(false)
    private val _isPremiumSeen = MutableStateFlow(false)
    
    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _monthlyMealScansUsed = MutableStateFlow(0)
    val monthlyMealScansUsed: StateFlow<Int> = _monthlyMealScansUsed.asStateFlow()

    private val _weeklyReportsUsed = MutableStateFlow(0)
    val weeklyReportsUsed: StateFlow<Int> = _weeklyReportsUsed.asStateFlow()

    private val _dailyAiCoachMessagesUsed = MutableStateFlow(0)
    val dailyAiCoachMessagesUsed: StateFlow<Int> = _dailyAiCoachMessagesUsed.asStateFlow()
    
    private val _reminderNotificationsEnabled = MutableStateFlow(true)
    val reminderNotificationsEnabled: StateFlow<Boolean> = _reminderNotificationsEnabled.asStateFlow()
    
    private val _waterReminderEnabled = MutableStateFlow(true)
    val waterReminderEnabled: StateFlow<Boolean> = _waterReminderEnabled.asStateFlow()
    
    private val _weightReminderEnabled = MutableStateFlow(false)
    val weightReminderEnabled: StateFlow<Boolean> = _weightReminderEnabled.asStateFlow()

    // Date navigation "yyyy-MM-dd"
    private val _selectedDate = MutableStateFlow(getCurrentDateLabel())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Main user state
    val loggedInUser: StateFlow<UserEntity?> = repository.observeLoggedInUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val mealsForSelectedDate: StateFlow<List<MealLogEntity>> = combine(loggedInUser, _selectedDate) { user, date ->
        Pair(user?.email ?: "", date)
    }.flatMapLatest { (email, date) ->
        if (email.isEmpty()) flowOf(emptyList())
        else repository.observeMealsForDay(email, date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val waterForSelectedDate: StateFlow<List<WaterLogEntity>> = combine(loggedInUser, _selectedDate) { user, date ->
        Pair(user?.email ?: "", date)
    }.flatMapLatest { (email, date) ->
        if (email.isEmpty()) flowOf(emptyList())
        else repository.observeWaterForDay(email, date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val weightLogs: StateFlow<List<WeightLogEntity>> = loggedInUser.flatMapLatest { user ->
        val email = user?.email ?: ""
        if (email.isEmpty()) flowOf(emptyList())
        else repository.observeWeightHistory(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val mealPresets: StateFlow<List<MealPresetEntity>> = loggedInUser.flatMapLatest { user ->
        val email = user?.email ?: ""
        if (email.isEmpty()) flowOf(emptyList())
        else repository.observeMealPresets(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val userStreak: StateFlow<StreakEntity?> = loggedInUser.flatMapLatest { user ->
        val email = user?.email ?: ""
        if (email.isEmpty()) flowOf(null)
        else repository.observeStreak(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI actions and search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<FoodItem>> = _searchQuery
        .map { query -> FoodCatalog.search(query) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FoodCatalog.items)

    // AI Quick Add states
    private val _isScanningAi = MutableStateFlow(false)
    val isScanningAi: StateFlow<Boolean> = _isScanningAi.asStateFlow()
    
    private val _selectedImageBitmap = MutableStateFlow<android.graphics.Bitmap?>(null)
    val selectedImageBitmap: StateFlow<android.graphics.Bitmap?> = _selectedImageBitmap.asStateFlow()

    private val _aiDetectedFoods = MutableStateFlow<List<DetectedFood>>(emptyList())
    val aiDetectedFoods: StateFlow<List<DetectedFood>> = _aiDetectedFoods.asStateFlow()

    private val _aiError = MutableStateFlow<String?>(null)
    val aiError: StateFlow<String?> = _aiError.asStateFlow()

    private val _premiumGateFeature = MutableStateFlow<String?>(null)
    val premiumGateFeature: StateFlow<String?> = _premiumGateFeature.asStateFlow()

    fun triggerPremiumGate(feature: String) {
        analyticsManager.logPremiumGateShown(feature)
        analyticsManager.logPremiumFeatureBlocked(feature)
        _premiumGateFeature.value = feature
    }

    fun dismissPremiumGate() {
        _premiumGateFeature.value = null
    }

    // AI Fitness Tips states
    private val _fitnessTip = MutableStateFlow<String?>(null)
    val fitnessTip: StateFlow<String?> = _fitnessTip.asStateFlow()
    
    private val _isFetchingTip = MutableStateFlow(false)
    val isFetchingTip: StateFlow<Boolean> = _isFetchingTip.asStateFlow()

    // AI Weekly Summary
    private val _weeklySummary = MutableStateFlow<String?>(null)
    val weeklySummary: StateFlow<String?> = _weeklySummary.asStateFlow()

    private val _isFetchingWeeklySummary = MutableStateFlow(false)
    val isFetchingWeeklySummary: StateFlow<Boolean> = _isFetchingWeeklySummary.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val chatMessages: StateFlow<List<ChatMessageEntity>> = loggedInUser.flatMapLatest { user ->
        val email = user?.email ?: ""
        if (email.isEmpty()) flowOf(emptyList())
        else repository.observeChatMessages(email)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isChatbotThinking = MutableStateFlow(false)
    val isChatbotThinking: StateFlow<Boolean> = _isChatbotThinking.asStateFlow()

    fun sendMessageToCoach(message: String) {
        val user = loggedInUser.value ?: return
        if (message.isBlank()) return

        if (!checkPremiumFeatureAccess("AI Coach")) {
            triggerPremiumGate("AI Coach")
            val currentPlan = if (_isPremium.value) "pro" else "free"
            analyticsManager.logAiCoachLimitReached(currentPlan, _dailyAiCoachMessagesUsed.value)
            return
        }
        
        viewModelScope.launch {
            // Immediately insert user msg
            repository.insertChatMessage(
                ChatMessageEntity(email = user.email, role = "user", content = message)
            )

            _isChatbotThinking.value = true
            
            // Build context
            val history = repository.observeChatMessages(user.email).first()
            val recentMeals = repository.observeMealsForDay(user.email, getCurrentDateLabel()).first()
            val totalCal = recentMeals.sumOf { it.calories.toDouble() }.toInt()
            
            val contextPrefix = """
                You are a personalized AI Nutrition & Fitness Coach. The user's name is ${user.name}.
                Goal: ${user.goalType}. Target daily calories: ${user.dailyCalorieGoal} kcal.
                Today's tracked calories so far: $totalCal kcal.
                Use previous conversation history if relevant. Provide short, concise, and highly encouraging advice (2-3 sentences max). Ask follow-up questions to keep engagement high.
            """.trimIndent()
            
            val responseText = try {
                val reply = com.example.data.api.GeminiClient.sendCoachMessage(contextPrefix, history, message)
                incrementAiCoachMessages()
                val currentPlan = if (_isPremium.value) "pro" else "free"
                analyticsManager.logAiCoachMessageSent(currentPlan, _dailyAiCoachMessagesUsed.value)
                analyticsManager.logCoachMessageSent()
                reply
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "AI Coach API failure")
                "AI Coach is temporarily unavailable. Please try again later."
            }
            
            repository.insertChatMessage(
                ChatMessageEntity(email = user.email, role = "model", content = responseText)
            )

            _isChatbotThinking.value = false
        }
    }

    fun generateWeeklySummary() {
        val user = loggedInUser.value ?: return
        
        if (!checkPremiumFeatureAccess("Weekly Report")) {
            triggerPremiumGate("Weekly Report")
            return
        }

        if (_weeklySummary.value != null || _isFetchingWeeklySummary.value) return

        _isFetchingWeeklySummary.value = true
        viewModelScope.launch {
            // Fetch 7 days of actual data from Room
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -7)
            val weekAgoTs = cal.timeInMillis

            val recentMeals = repository.getMealLogsSince(user.email, weekAgoTs)
            val recentWater = repository.getWaterLogsSince(user.email, weekAgoTs)
            val recentWeight = repository.getWeightLogsSince(user.email, weekAgoTs)

            val totalCals = recentMeals.sumOf { it.calories.toDouble() }
            val totalProtein = recentMeals.sumOf { it.protein.toDouble() }
            val avgCals = if (recentMeals.isNotEmpty()) (totalCals / 7).toInt() else 0
            val avgProtein = if (recentMeals.isNotEmpty()) (totalProtein / 7).toInt() else 0

            val waterDaysLogged = recentWater.groupBy { it.dateLabel }.size

            val weightStr = if (recentWeight.isNotEmpty()) {
                val startWt = recentWeight.first().weightKg
                val endWt = recentWeight.last().weightKg
                "Start weight: $startWt kg, End weight: $endWt kg"
            } else "No weight logged this week."

            val dailyTargetCals = user.dailyCalorieGoal
            val dailyTargetProtein = user.dailyProteinGoalG

            val metricsData = """
                - User Goals: $dailyTargetCals kcal, $dailyTargetProtein g protein
                - Average Intake: $avgCals kcal, $avgProtein g protein
                - Days logged water: $waterDaysLogged out of 7
                - Weight Changes: $weightStr
                - Total Meals Tracked: ${recentMeals.size}
            """.trimIndent()

            try {
                val tip = com.example.data.api.GeminiClient.fetchWeeklySummary(metricsData)
                _weeklySummary.value = tip
                incrementWeeklyReports()
                analyticsManager.logWeeklyReportGenerated()
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "Weekly Report API failure")
                _weeklySummary.value = "Unable to generate weekly report right now."
            } finally {
                _isFetchingWeeklySummary.value = false
            }
        }
    }

    init {
        analyticsManager.onAnalyticsError = { e -> crashlyticsManager.recordException(e, "Analytics failure") }
        ensureFirebaseInit()
        viewModelScope.launch {
            getApplication<Application>().dataStore.data.collect { prefs ->
                _isAppTourCompleted.value = prefs[appTourCompletedKey] ?: false
                _isPremiumSeen.value = prefs[premiumSeenKey] ?: false
                _isDarkMode.value = prefs[darkModeKey] ?: true
                _reminderNotificationsEnabled.value = prefs[reminderNotificationsKey] ?: true
                _waterReminderEnabled.value = prefs[waterReminderKey] ?: true
                _weightReminderEnabled.value = prefs[weightReminderKey] ?: false
                _isPremium.value = prefs[isPremiumKey] ?: false
                
                // Track usage usage across resets
                val currentMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                val lastMonthStr = prefs[monthlyMealScansMonthStrKey] ?: ""
                
                if (currentMonthStr != lastMonthStr) {
                    // Reset meal scans for the new month
                    _monthlyMealScansUsed.value = 0
                    getApplication<Application>().dataStore.edit { editPrefs ->
                        editPrefs[monthlyMealScansMonthStrKey] = currentMonthStr
                        editPrefs[monthlyMealScansKey] = 0
                    }
                } else {
                    _monthlyMealScansUsed.value = prefs[monthlyMealScansKey] ?: 0
                }

                // Reset Weekly Reports
                val currentWeekStr = SimpleDateFormat("yyyy-ww", Locale.getDefault()).format(Date())
                val lastWeekStr = prefs[weeklyReportsWeekStrKey] ?: ""
                try {
                    if (currentWeekStr != lastWeekStr) {
                        _weeklyReportsUsed.value = 0
                        getApplication<Application>().dataStore.edit { editPrefs ->
                            editPrefs[weeklyReportsWeekStrKey] = currentWeekStr
                            editPrefs[weeklyReportsRunKey] = 0
                        }
                    } else {
                        _weeklyReportsUsed.value = prefs[weeklyReportsRunKey] ?: 0
                    }
                } catch (e: Exception) {
                    crashlyticsManager.recordException(e, "DataStore write failed for Weekly Reports resets")
                }

                // Reset AI Coach Messages
                val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val lastCoachResetStr = prefs[dailyAiCoachResetDateKey] ?: ""
                try {
                    if (todayDateStr != lastCoachResetStr) {
                        _dailyAiCoachMessagesUsed.value = 0
                        getApplication<Application>().dataStore.edit { editPrefs ->
                            editPrefs[dailyAiCoachResetDateKey] = todayDateStr
                            editPrefs[dailyAiCoachMessagesKey] = 0
                        }
                    } else {
                        _dailyAiCoachMessagesUsed.value = prefs[dailyAiCoachMessagesKey] ?: 0
                    }
                } catch (e: Exception) {
                    crashlyticsManager.recordException(e, "Daily reset fails for AI Coach")
                }
            }
        }
        viewModelScope.launch {
            loggedInUser.collect { user ->
                if (user != null) {
                    val appVersion = "1.0"
                    // we guess provider given our app uses simple google check or pass
                    val authProvider = if (user.name.contains("User") && user.email.isNotEmpty()) "password" else "google" 
                    val currentPlan = if (_isPremiumSeen.value) "pro" else "free"
                    crashlyticsManager.setCustomKeys(
                        currentPlan = currentPlan,
                        goalType = user.goalType,
                        activityLevel = user.activityLevel,
                        authProvider = authProvider,
                        appVersion = appVersion
                    )
                }
            }
        }
        
        viewModelScope.launch {
            // Check auth state
            val fbUser = auth.currentUser
            if (fbUser == null) {
                _currentScreen.value = Screen.Login
            } else {
                fbUser.reload().addOnCompleteListener {
                    if (!fbUser.isEmailVerified) {
                        _currentScreen.value = Screen.VerifyEmail
                    } else {
                        // Check if Room has a user profile
                        viewModelScope.launch {
                            val user = repository.getLoggedInUser()
                            if (user == null) {
                                _currentScreen.value = Screen.SetupGoals
                            } else {
                                if (!_isAppTourCompleted.value) {
                                    _currentScreen.value = Screen.AppTour
                                } else if (!_isPremiumSeen.value) {
                                    _currentScreen.value = Screen.PremiumIntro
                                } else {
                                    _currentScreen.value = Screen.Dashboard
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun ensureFirebaseInit() {
        if (FirebaseApp.getApps(getApplication<Application>()).isEmpty()) {
            FirebaseApp.initializeApp(getApplication<Application>())
        }
    }

    // --- Presets Actions ---
    fun saveAsPreset(presetName: String, mealType: String, foods: List<DetectedFood>) {
        val user = loggedInUser.value ?: return
        if (foods.isEmpty() || presetName.isBlank()) return

        // Aggregate food stats
        val totalCal = foods.sumOf { it.calories.toDouble() }.toFloat()
        val totalPro = foods.sumOf { it.protein.toDouble() }.toFloat()
        val totalCarb = foods.sumOf { it.carbs.toDouble() }.toFloat()
        val totalFat = foods.sumOf { it.fats.toDouble() }.toFloat()
        val totalFib = foods.sumOf { it.fiber.toDouble() }.toFloat()

        viewModelScope.launch {
            repository.insertMealPreset(
                MealPresetEntity(
                    email = user.email,
                    presetName = presetName.trim(),
                    mealType = mealType,
                    calories = totalCal,
                    protein = totalPro,
                    carbs = totalCarb,
                    fats = totalFat,
                    fiber = totalFib
                )
            )
            analyticsManager.logMealPresetSaved()
        }
    }

    fun logPreset(preset: MealPresetEntity, selectedMealType: String = preset.mealType) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            val log = MealLogEntity(
                email = user.email,
                foodName = preset.presetName, // using preset name as the aggregated food name
                mealType = selectedMealType,
                quantityMultiplier = 1f,
                quantityUnit = "portion",
                calories = preset.calories,
                protein = preset.protein,
                carbs = preset.carbs,
                fats = preset.fats,
                fiber = preset.fiber,
                dateLabel = _selectedDate.value
            )
            repository.insertMealLog(log)
            analyticsManager.logMealPresetUsed()
            analyticsManager.logMealLogged(selectedMealType)
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            repository.deleteMealPresetById(id)
        }
    }

    // --- Helpers ---
    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    fun toggleTheme() {
        val newState = !_isDarkMode.value
        _isDarkMode.value = newState
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[darkModeKey] = newState
            }
        }
    }
    
    fun toggleReminderNotifications() {
        val newState = !_reminderNotificationsEnabled.value
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[reminderNotificationsKey] = newState
            }
        }
    }
    
    fun toggleWaterReminder() {
        val newState = !_waterReminderEnabled.value
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[waterReminderKey] = newState
            }
        }
    }
    
    fun toggleWeightReminder() {
        val newState = !_weightReminderEnabled.value
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[weightReminderKey] = newState
            }
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun searchFoods(query: String) {
        _searchQuery.value = query
    }

    // --- Authentication Actions ---
    fun loginWithEmail(email: String, pass: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    analyticsManager.logLoginSuccess()
                    onSuccess()
                    checkPostAuthFlow()
                } else {
                    onFailure(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun registerWithEmail(email: String, pass: String, name: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fbUser = auth.currentUser
                    fbUser?.sendEmailVerification()
                    
                    viewModelScope.launch {
                        val newUser = UserEntity(
                            email = email.trim(),
                            name = name.trim(),
                            isLoggedIn = true
                        )
                        repository.updateProfile(newUser)
                        analyticsManager.logSignupCompleted("password")
                        onSuccess()
                        checkPostAuthFlow()
                    }
                } else {
                    onFailure(task.exception?.message ?: "Signup failed")
                }
            }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        if (email.isBlank()) {
            onFailure("Email cannot be empty")
            return
        }
        auth.sendPasswordResetEmail(email.trim())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    analyticsManager.logPasswordResetRequested()
                    onSuccess()
                }
                else {
                    task.exception?.let { crashlyticsManager.recordException(it, "Forgot password failure") }
                    onFailure(task.exception?.message ?: "Reset failed")
                }
            }
    }

    fun sendVerificationEmail(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    task.exception?.let { crashlyticsManager.recordException(it, "Email verification failure") }
                    onFailure(task.exception?.message ?: "Failed to send email")
                }
            }
        } else {
            onFailure("No user found")
        }
    }

    fun checkVerificationStatus(onComplete: () -> Unit) {
        val user = auth.currentUser
        user?.reload()?.addOnCompleteListener {
            if (user.isEmailVerified) {
                analyticsManager.logEmailVerified()
                checkPostAuthFlow()
            }
            onComplete()
        }
    }

    fun markAppTourCompleted() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[appTourCompletedKey] = true
            }
            analyticsManager.logAppTourCompleted()
            checkPostAuthFlow()
        }
    }

    fun incrementMonthlyMealScans() {
        viewModelScope.launch {
            val newVal = _monthlyMealScansUsed.value + 1
            _monthlyMealScansUsed.value = newVal
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[monthlyMealScansKey] = newVal
            }
        }
    }

    fun incrementWeeklyReports() {
        viewModelScope.launch {
            val newVal = _weeklyReportsUsed.value + 1
            _weeklyReportsUsed.value = newVal
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[weeklyReportsRunKey] = newVal
            }
        }
    }

    fun incrementAiCoachMessages() {
        viewModelScope.launch {
            try {
                val newVal = _dailyAiCoachMessagesUsed.value + 1
                _dailyAiCoachMessagesUsed.value = newVal
                getApplication<Application>().dataStore.edit { prefs ->
                    prefs[dailyAiCoachMessagesKey] = newVal
                }
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "DataStore write fails for AI Coach usage tracking fails")
            }
        }
    }

    fun checkPremiumFeatureAccess(featureName: String): Boolean {
        if (_isPremium.value) return true
        
        return when (featureName) {
            "Meal Scan" -> _monthlyMealScansUsed.value < 10
            "Weekly Report" -> _weeklyReportsUsed.value < 1
            "AI Coach" -> _dailyAiCoachMessagesUsed.value < 2
            else -> false
        }
    }

    fun markPremiumSeen() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[premiumSeenKey] = true
            }
            analyticsManager.logPremiumIntroViewed()
            checkPostAuthFlow()
        }
    }

    fun continueWithFree() {
        analyticsManager.logContinueFreeClicked()
        markPremiumSeen()
    }

    fun upgradeToPro() {
        analyticsManager.logUpgradeProClicked()
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[isPremiumKey] = true
                prefs[premiumSeenKey] = true
            }
            _isPremium.value = true
        }
        _currentScreen.value = Screen.Dashboard
    }

    fun signInWithGoogle(context: Context, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credentialManager = CredentialManager.create(context)
        val serverClientIdId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (serverClientIdId == 0) {
            onFailure("Missing default_web_client_id in resources")
            return
        }
        val serverClientId = context.getString(serverClientIdId)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()
            
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
            
        viewModelScope.launch {
            try {
                val result = credentialManager.getCredential(context = context, request = request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                    auth.signInWithCredential(authCredential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = task.result?.user
                            if (user != null) {
                                viewModelScope.launch {
                                    val newUser = UserEntity(
                                        email = user.email ?: "",
                                        name = user.displayName ?: "User",
                                        isLoggedIn = true
                                    )
                                    repository.updateProfile(newUser)
                                    analyticsManager.logGoogleLoginSuccess()
                                    checkPostAuthFlow()
                                    onSuccess()
                                }
                            }
                        } else {
                            task.exception?.let { crashlyticsManager.recordException(it, "Google Sign-In Firebase Auth failure") }
                            onFailure(task.exception?.message ?: "Firebase Auth failed")
                        }
                    }
                } else {
                    crashlyticsManager.recordExceptionMessage("Google Sign-In Unexpected credential type")
                    onFailure("Unexpected credential type")
                }
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "Google Sign-In failure")
                onFailure("Google Sign In failed: ${e.message}")
            }
        }
    }

    fun checkPostAuthFlow() {
        val fbUser = auth.currentUser
        if (fbUser == null) {
            _currentScreen.value = Screen.Login
            return
        }
        fbUser.reload().addOnCompleteListener {
            if (!fbUser.isEmailVerified) {
                _currentScreen.value = Screen.VerifyEmail
            } else {
                viewModelScope.launch {
                    val user = repository.getLoggedInUser()
                    // Re-fetch current user locally if needed (it should match fbUser.email)
                    if (user != null && fbUser.email != user.email) {
                        repository.logoutUser(user.email)
                    }

                    if (user == null || !user.isLoggedIn || (user.age == 25 && user.gender == "Male" && user.activityLevel == "Sedentary" && user.targetWeightKg == 70f)) {
                        _currentScreen.value = Screen.SetupGoals
                    } else if (!_isAppTourCompleted.value) {
                        _currentScreen.value = Screen.AppTour
                    } else if (!_isPremiumSeen.value) {
                        _currentScreen.value = Screen.PremiumIntro
                    } else {
                        _currentScreen.value = Screen.Dashboard
                    }
                }
            }
        }
    }

    fun logout() {
        auth.signOut()
        analyticsManager.logLogout()
        viewModelScope.launch {
            val email = loggedInUser.value?.email ?: ""
            if (email.isNotEmpty()) {
                repository.logoutUser(email)
            }
            _currentScreen.value = Screen.Login
        }
    }

    fun deleteAccount(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val emailStr = user.email ?: ""
            user.delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        if (emailStr.isNotEmpty()) {
                            repository.deleteUserData(emailStr)
                        }
                        getApplication<Application>().dataStore.edit { it.clear() }
                        auth.signOut()
                        val currentPlan = if (_isPremiumSeen.value) "pro" else "free"
                        analyticsManager.logAccountDeleted(currentPlan)
                        _currentScreen.value = Screen.Login
                        onSuccess()
                    }
                } else {
                    // Sometimes Firebase requires re-authentication to delete an account
                    onFailure(task.exception?.message ?: "Failed to delete account")
                }
            }
        } else {
            onFailure("No authenticated user found")
        }
    }

    // --- Profile Setup Calculator Actions ---
    fun saveUserGoals(
        gender: String,
        age: Int,
        heightCm: Float,
        weightKg: Float,
        activityLevel: String,
        goalType: String,
        customTargetWeightKg: Float? = null,
        isFromOnboarding: Boolean = false
    ) {
        val email = loggedInUser.value?.email ?: "user@test.com"
        val name = loggedInUser.value?.name ?: "User"

        // Perform Mifflin-St Jeor calculations
        val bmr = if (gender == "Male") {
            (10f * weightKg) + (6.25f * heightCm) - (5f * age) + 5f
        } else {
            (10f * weightKg) + (6.25f * heightCm) - (5f * age) - 161f
        }

        val multiplier = when (activityLevel) {
            "Sedentary" -> 1.2f
            "Lightly Active" -> 1.375f
            "Moderately Active" -> 1.55f
            "Very Active" -> 1.725f
            "Athlete" -> 1.9f
            else -> 1.2f
        }

        val tdee = bmr * multiplier

        val dailyCalorieGoal = when (goalType) {
            "Lose Weight" -> (tdee - 500f).coerceAtLeast(1200f).toInt()
            "Gain Weight" -> (tdee + 500f).toInt()
            "Muscle Gain" -> (tdee + 300f).toInt()
            else -> tdee.toInt()
        }
        
        val targetWeightKg = customTargetWeightKg ?: when (goalType) {
            "Lose Weight" -> weightKg - 2f // Simple target for visual diff
            "Gain Weight", "Muscle Gain" -> weightKg + 2f
            else -> weightKg
        }

        // Standard dynamic macro split
        val proteinP = if (goalType == "Muscle Gain" || goalType == "Lose Weight") 0.35f else 0.20f
        val fatsP = 0.30f
        val carbsP = 1.0f - proteinP - fatsP
        val proteinG = ((dailyCalorieGoal * proteinP) / 4f).toInt()
        val carbsG = ((dailyCalorieGoal * carbsP) / 4f).toInt()
        val fatsG = ((dailyCalorieGoal * fatsP) / 9f).toInt()
        val fiberG = if (dailyCalorieGoal < 2000) 25 else 30
        val waterMl = (weightKg * 35).toInt().coerceAtLeast(2000) // 35ml per kg body weight

        val updatedUser = UserEntity(
            email = email,
            name = name,
            heightCm = heightCm,
            weightKg = weightKg,
            age = age,
            gender = gender,
            activityLevel = activityLevel,
            goalType = goalType,
            targetWeightKg = targetWeightKg,
            dailyCalorieGoal = dailyCalorieGoal,
            dailyProteinGoalG = proteinG,
            dailyCarbsGoalG = carbsG,
            dailyFatsGoalG = fatsG,
            dailyFiberGoalG = fiberG,
            dailyWaterGoalMl = waterMl,
            isLoggedIn = true
        )

        viewModelScope.launch {
            val oldUser = repository.getLoggedInUser()
            repository.updateProfile(updatedUser)
            
            if (isFromOnboarding) {
                analyticsManager.logProfileSetupCompleted(goalType, activityLevel)
            } else {
                analyticsManager.logProfileUpdated()
                if (oldUser?.goalType != goalType) {
                    analyticsManager.logGoalChanged(goalType)
                }
            }
            
            // Log weight instantly as our start weight for analytics
            repository.insertWeightLog(
                WeightLogEntity(
                    email = email,
                    weightKg = weightKg,
                    dateLabel = getCurrentDateLabel()
                )
            )
            checkPostAuthFlow()
        }
    }

    // --- Logging Manual Foods ---
    fun logManualFood(item: FoodItem, quantityMultiplierValue: Float, mealType: String) {
        val user = loggedInUser.value ?: return

        // Multiplier scale calculations
        val scale = when (item.baseQuantityUnit) {
            "g", "ml" -> quantityMultiplierValue / item.baseQuantityVal
            else -> quantityMultiplierValue // directly mapping piece(s), plate, cup, etc.
        }

        val log = MealLogEntity(
            email = user.email,
            foodName = item.name,
            mealType = mealType,
            quantityMultiplier = quantityMultiplierValue,
            quantityUnit = item.baseQuantityUnit,
            calories = item.baseCalories * scale,
            protein = item.baseProtein * scale,
            carbs = item.baseCarbs * scale,
            fats = item.baseFats * scale,
            fiber = item.baseFiber * scale,
            dateLabel = _selectedDate.value
        )

        viewModelScope.launch {
            repository.insertMealLog(log)
            analyticsManager.logMealLogged(mealType)
        }
    }

    fun deleteLoggedMeal(meal: MealLogEntity) {
        viewModelScope.launch {
            repository.deleteMealLog(meal)
            analyticsManager.logMealDeleted()
        }
    }

    // --- Water Log Actions ---
    fun addWater(amountMl: Int) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            repository.insertWaterLog(
                WaterLogEntity(
                    email = user.email,
                    amountMl = amountMl,
                    dateLabel = _selectedDate.value
                )
            )
            analyticsManager.logWaterLogged()
        }
    }

    fun deleteWaterLog(id: Long) {
        viewModelScope.launch {
            repository.deleteWaterLogById(id)
        }
    }

    fun clearAllWaterForDay() {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            repository.clearWaterForDay(user.email, _selectedDate.value)
        }
    }

    // --- Weight Log Actions ---
    fun updateWeight(weightKg: Float) {
        val user = loggedInUser.value ?: return
        viewModelScope.launch {
            try {
                repository.insertWeightLog(
                    WeightLogEntity(
                        email = user.email,
                        weightKg = weightKg,
                        dateLabel = getCurrentDateLabel()
                    )
                )
                // Update base profile weight as well
                repository.updateProfile(user.copy(weightKg = weightKg))
                analyticsManager.logWeightLogged()
                registerWeightStreak()
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "Weight tracking failure")
            }
        }
    }

    fun deleteWeightLog(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteWeightLogById(id)
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "Weight tracking failure")
            }
        }
    }

    // --- AI Quick Add Actions ---
    fun setSelectedImage(bitmap: android.graphics.Bitmap?) {
        _selectedImageBitmap.value = bitmap
    }

    fun clearAiBuffer() {
        _aiDetectedFoods.value = emptyList()
        _aiError.value = null
        _selectedImageBitmap.value = null
    }

    fun checkAndAnalyzeMealPhoto(bitmap: android.graphics.Bitmap, userDescription: String) {
        if (!checkPremiumFeatureAccess("Meal Scan")) {
            triggerPremiumGate("Meal Scan")
            return
        }

        _isScanningAi.value = true
        _aiError.value = null
        _aiDetectedFoods.value = emptyList()

        viewModelScope.launch {
            try {
                val detected = repository.parsePhotoWithGemini(bitmap, userDescription)
                if (detected.isNotEmpty()) {
                    _aiDetectedFoods.value = detected
                    analyticsManager.logMealScanned()
                    incrementMonthlyMealScans()
                } else {
                    _aiError.value = "Unable to analyze meal right now. Please check your internet connection and try again."
                }
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "Meal Scan API failure")
                _aiError.value = "Unable to analyze meal right now. Please check your internet connection and try again."
            } finally {
                _isScanningAi.value = false
            }
        }
    }

    fun checkAndAnalyzeMealText(mealText: String) {
        if (mealText.trim().isEmpty()) return
        val user = loggedInUser.value ?: return

        if (!checkPremiumFeatureAccess("Meal Scan")) {
            triggerPremiumGate("Meal Scan")
            return
        }

        _isScanningAi.value = true
        _aiError.value = null
        _aiDetectedFoods.value = emptyList()

        viewModelScope.launch {
            try {
                val detected = repository.parseWithGemini(mealText)
                if (detected.isNotEmpty()) {
                    _aiDetectedFoods.value = detected
                    incrementMonthlyMealScans()
                } else {
                    _aiError.value = "Unable to analyze meal right now. Please check your internet connection and try again."
                }
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "Text Scan API failure")
                _aiError.value = "Unable to analyze meal right now. Please check your internet connection and try again."
            } finally {
                _isScanningAi.value = false
            }
        }
    }

    fun confirmAndLogAiMeals(mealType: String) {
        val user = loggedInUser.value ?: return
        val foods = _aiDetectedFoods.value
        if (foods.isEmpty()) return

        viewModelScope.launch {
            for (food in foods) {
                val log = MealLogEntity(
                    email = user.email,
                    foodName = food.foodName,
                    mealType = mealType,
                    quantityMultiplier = food.quantityMultiplier,
                    quantityUnit = food.quantityUnit,
                    calories = food.calories,
                    protein = food.protein,
                    carbs = food.carbs,
                    fats = food.fats,
                    fiber = food.fiber,
                    dateLabel = _selectedDate.value
                )
                repository.insertMealLog(log)
            }
            analyticsManager.logMealLogged(mealType)
            // Clear buffer
            _aiDetectedFoods.value = emptyList()
            _selectedImageBitmap.value = null
            // Back to dashboard
            _currentScreen.value = Screen.Dashboard
        }
    }

    fun updateAiDetectedFood(index: Int, updated: DetectedFood) {
        val currentList = _aiDetectedFoods.value.toMutableList()
        if (index in currentList.indices) {
            currentList[index] = updated
            _aiDetectedFoods.value = currentList
            analyticsManager.logMealScanEdited()
        }
    }

    fun deleteAiDetectedFood(index: Int) {
        val currentList = _aiDetectedFoods.value.toMutableList()
        if (index in currentList.indices) {
            currentList.removeAt(index)
            _aiDetectedFoods.value = currentList
            analyticsManager.logMealScanEdited()
        }
    }

    fun clearAiMealsBuffer() {
        _aiDetectedFoods.value = emptyList()
        _aiError.value = null
    }

    fun fetchDailyFitnessTip() {
        if (_fitnessTip.value != null || _isFetchingTip.value) return // already fetched or fetching
        _isFetchingTip.value = true
        viewModelScope.launch {
            try {
                val tip = com.example.data.api.GeminiClient.fetchDailyFitnessTip()
                _fitnessTip.value = tip
                analyticsManager.logFitnessTipViewed()
            } catch (e: Exception) {
                crashlyticsManager.recordException(e, "Fitness Tip API failure")
                _fitnessTip.value = "Couldn't load today's fitness tip."
            } finally {
                _isFetchingTip.value = false
            }
        }
    }


    // Global helper formatters
    fun getCurrentDateLabel(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getYesterdayDateLabel(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return sdf.format(cal.time)
    }

    private fun updateStreakInternal(
        streak: StreakEntity,
        type: String, // "login", "protein", "water", "weight"
        today: String,
        yesterday: String
    ): StreakEntity {
        var currentStreak = when(type) {
            "login" -> streak.loginStreak
            "protein" -> streak.proteinStreak
            "water" -> streak.waterStreak
            "weight" -> streak.weightStreak
            else -> 0
        }
        val lastDate = when(type) {
            "login" -> streak.lastLoginDate
            "protein" -> streak.lastProteinDate
            "water" -> streak.lastWaterDate
            "weight" -> streak.lastWeightDate
            else -> ""
        }

        if (lastDate == today) return streak // Already updated today

        if (lastDate == yesterday) {
            currentStreak += 1
        } else {
            currentStreak = 1
        }

        return when(type) {
            "login" -> streak.copy(loginStreak = currentStreak, lastLoginDate = today)
            "protein" -> streak.copy(proteinStreak = currentStreak, lastProteinDate = today)
            "water" -> streak.copy(waterStreak = currentStreak, lastWaterDate = today)
            "weight" -> streak.copy(weightStreak = currentStreak, lastWeightDate = today)
            else -> streak
        }
    }

    fun checkAndApplyStreaks(proteinMet: Boolean, waterMet: Boolean) {
        val user = loggedInUser.value ?: return
        val email = user.email
        val today = getCurrentDateLabel()
        val yesterday = getYesterdayDateLabel()

        viewModelScope.launch {
            var currentStreak = repository.getStreak(email) ?: StreakEntity(email = email)
            currentStreak = updateStreakInternal(currentStreak, "login", today, yesterday)
            
            if (proteinMet) {
                currentStreak = updateStreakInternal(currentStreak, "protein", today, yesterday)
            }
            if (waterMet) {
                currentStreak = updateStreakInternal(currentStreak, "water", today, yesterday)
            }
            repository.insertOrUpdateStreak(currentStreak)
        }
    }

    private fun registerWeightStreak() {
        val user = loggedInUser.value ?: return
        val today = getCurrentDateLabel()
        val yesterday = getYesterdayDateLabel()
        viewModelScope.launch {
            var currentStreak = repository.getStreak(user.email) ?: StreakEntity(email = user.email)
            currentStreak = updateStreakInternal(currentStreak, "weight", today, yesterday)
            repository.insertOrUpdateStreak(currentStreak)
        }
    }

    fun getFormattedSelectedDate(): String {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            val date = parser.parse(_selectedDate.value)
            if (date != null) {
                // Check if it matches today or yesterday for beautiful UX
                val today = parser.format(Date())
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -1)
                val yesterday = parser.format(cal.time)

                return when (_selectedDate.value) {
                    today -> "Today, " + formatter.format(date)
                    yesterday -> "Yesterday, " + formatter.format(date)
                    else -> formatter.format(date)
                }
            }
        } catch (e: Exception) {
            // Ignore error, fallback to string
        }
        return _selectedDate.value
    }

    fun stepDate(days: Int) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = parser.parse(_selectedDate.value) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = date
            cal.add(Calendar.DAY_OF_YEAR, days)
            _selectedDate.value = parser.format(cal.time)
        } catch (e: Exception) {
            // error
        }
    }
}
