package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String, // Treat email as identifier
    val name: String,
    val heightCm: Float = 170f,
    val weightKg: Float = 70f,
    val age: Int = 25,
    val gender: String = "Male", // "Male", "Female"
    val activityLevel: String = "Sedentary", // "Sedentary", "Lightly Active", "Moderately Active", "Very Active"
    val goalType: String = "Maintain Weight", // "Lose Weight", "Maintain Weight", "Gain Weight", "Muscle Gain"
    val targetWeightKg: Float = 70f,
    val dailyCalorieGoal: Int = 2000,
    val dailyProteinGoalG: Int = 60,
    val dailyCarbsGoalG: Int = 250,
    val dailyFatsGoalG: Int = 55,
    val dailyFiberGoalG: Int = 25,
    val dailyWaterGoalMl: Int = 2500,
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_logs")
data class MealLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val foodName: String,
    val mealType: String, // "Breakfast", "Lunch", "Snacks", "Dinner"
    val quantityMultiplier: Float, // e.g., 2.0 (for 2 pieces, or 2x 100g)
    val quantityUnit: String, // "pieces", "g", "ml"
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val fiber: Float,
    val dateLabel: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_logs")
data class WaterLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val amountMl: Int,
    val dateLabel: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "weight_logs")
data class WeightLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val weightKg: Float,
    val dateLabel: String, // "yyyy-MM-dd"
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_streaks")
data class StreakEntity(
    @PrimaryKey val email: String,
    val loginStreak: Int = 0,
    val proteinStreak: Int = 0,
    val waterStreak: Int = 0,
    val weightStreak: Int = 0,
    val lastLoginDate: String = "",
    val lastProteinDate: String = "",
    val lastWaterDate: String = "",
    val lastWeightDate: String = ""
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_presets")
data class MealPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val presetName: String,
    val mealType: String, // "Breakfast", "Lunch", "Snacks", "Dinner"
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fats: Float,
    val fiber: Float,
    val timestamp: Long = System.currentTimeMillis()
)

