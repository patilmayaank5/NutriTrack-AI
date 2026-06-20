package com.example.data.repository

import android.util.Log
import com.example.data.api.DetectedFood
import com.example.data.api.GeminiClient
import com.example.data.database.*
import com.example.data.model.FoodCatalog
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*

class NutriRepository(private val dao: NutriDao) {

    // --- User flow ---
    val observeLoggedInUser: Flow<UserEntity?> = dao.observeLoggedInUser()

    suspend fun getLoggedInUser(): UserEntity? = dao.getLoggedInUser()

    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)

    suspend fun updateProfile(user: UserEntity) = dao.insertOrUpdateUser(user)

    suspend fun logoutUser(email: String) {
        val user = dao.getUserByEmail(email)
        if (user != null) {
            dao.insertOrUpdateUser(user.copy(isLoggedIn = false))
        }
    }

    suspend fun deleteUserData(email: String) {
        dao.deleteUser(email)
        dao.deleteAllMealLogs(email)
        dao.deleteAllWaterLogs(email)
        dao.deleteAllWeightLogs(email)
        dao.deleteAllMealPresets(email)
        dao.deleteUserStreaks(email)
        dao.clearChatHistory(email)
    }

    suspend fun loginUser(email: String): Boolean {
        val user = dao.getUserByEmail(email)
        return if (user != null) {
            dao.insertOrUpdateUser(user.copy(isLoggedIn = true))
            true
        } else {
            false
        }
    }

    // --- Meals flow ---
    fun observeMealsForDay(email: String, date: String): Flow<List<MealLogEntity>> =
        dao.observeMealsForDay(email, date)

    suspend fun insertMealLog(meal: MealLogEntity) = dao.insertMealLog(meal)

    suspend fun deleteMealLog(meal: MealLogEntity) = dao.deleteMealLog(meal)

    suspend fun deleteMealLogById(id: Long) = dao.deleteMealLogById(id)
    
    suspend fun getMealLogsSince(email: String, timestamp: Long) = dao.getMealLogsSince(email, timestamp)

    // --- Water flow ---
    fun observeWaterForDay(email: String, date: String): Flow<List<WaterLogEntity>> =
        dao.observeWaterForDay(email, date)

    suspend fun insertWaterLog(water: WaterLogEntity) = dao.insertWaterLog(water)

    suspend fun deleteWaterLogById(id: Long) = dao.deleteWaterLogById(id)

    suspend fun clearWaterForDay(email: String, date: String) = dao.clearWaterForDay(email, date)
    suspend fun getWaterLogsSince(email: String, timestamp: Long) = dao.getWaterLogsSince(email, timestamp)

    // --- Weight flow ---
    fun observeWeightHistory(email: String): Flow<List<WeightLogEntity>> =
        dao.observeWeightHistory(email)

    suspend fun insertWeightLog(weight: WeightLogEntity) = dao.insertWeightLog(weight)

    suspend fun deleteWeightLogById(id: Long) = dao.deleteWeightLogById(id)
    suspend fun getWeightLogsSince(email: String, timestamp: Long) = dao.getWeightLogsSince(email, timestamp)

    // --- Presets flow ---
    fun observeMealPresets(email: String): Flow<List<MealPresetEntity>> =
        dao.observeMealPresets(email)

    suspend fun insertMealPreset(preset: MealPresetEntity) = dao.insertMealPreset(preset)

    suspend fun deleteMealPresetById(id: Long) = dao.deleteMealPresetById(id)

    // --- Streak & Chat flow ---
    fun observeStreak(email: String): Flow<StreakEntity?> = dao.observeStreak(email)

    suspend fun getStreak(email: String): StreakEntity? = dao.getStreak(email)

    suspend fun insertOrUpdateStreak(streak: StreakEntity) = dao.insertOrUpdateStreak(streak)

    fun observeChatMessages(email: String): Flow<List<ChatMessageEntity>> = dao.observeChatMessages(email)

    suspend fun insertChatMessage(message: ChatMessageEntity) = dao.insertChatMessage(message)
    
    suspend fun clearChatHistory(email: String) = dao.clearChatHistory(email)

    // --- AI Parser ---
    suspend fun parseWithGemini(mealText: String): List<DetectedFood> {
        return GeminiClient.analyzeMealText(mealText)
    }

    suspend fun parsePhotoWithGemini(bitmap: android.graphics.Bitmap, userDescription: String?): List<DetectedFood> {
        val promptText = """
            You are a nutrition expert specializing in Indian food items.
            The user uploaded a photograph of their meal.
            ${if (!userDescription.isNullOrBlank()) "The user also described the photo as: \"$userDescription\"." else "Identify all visible food items from this photo."}
            Identify all distinct food items, estimate their quantity multipliers, quantity units, and standard realistic macro nutritional values (Calories, Protein in g, Carbs in g, Fats in g, and Fiber in g).
            Use standard Indian food items when possible.
            If the photo contains multiple items (e.g., roti, dal, and rice), you should split them into separate items:
            1. "Roti (Plain)", quantity: 2.0 piece(s), calories ~170 kcal, protein ~6g, carbs ~36g, fats ~1g, fiber ~5g.
            2. "Yellow Dal (Tadka)", quantity: 1.0 katori/cup, calories ~150 kcal, protein ~7g, carbs ~22g, fats ~4g, fiber ~5g.
            3. "Basmati Rice (Cooked)", quantity: 100.0 g, calories ~130 kcal, protein ~2.7g, carbs ~28g, fats ~0.3g, fiber ~0.4g.

            You MUST strictly return a JSON array matching this format (no conversational text surrounding, just valid JSON array):
            [
              {
                "foodName": "Food Name string",
                "quantityMultiplier": 1.0,
                "quantityUnit": "piece(s) / g / ml / katori/cup / plate",
                "calories": 150.0,
                "protein": 7.0,
                "carbs": 22.0,
                "fats": 4.0,
                "fiber": 5.0
              }
            ]
        """.trimIndent()

        return GeminiClient.analyzeMealPhoto(bitmap, promptText)
    }


}
