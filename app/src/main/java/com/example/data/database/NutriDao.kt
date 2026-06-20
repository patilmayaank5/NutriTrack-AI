package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NutriDao {

    // --- User Queries ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun observeUserByEmail(email: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    suspend fun getLoggedInUser(): UserEntity?

    @Query("SELECT * FROM users WHERE isLoggedIn = 1 LIMIT 1")
    fun observeLoggedInUser(): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    // --- Meal Log Queries ---
    @Query("SELECT * FROM meal_logs WHERE email = :email AND dateLabel = :date ORDER BY timestamp DESC")
    fun observeMealsForDay(email: String, date: String): Flow<List<MealLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealLog(meal: MealLogEntity)

    @Delete
    suspend fun deleteMealLog(meal: MealLogEntity)

    @Query("DELETE FROM meal_logs WHERE id = :id")
    suspend fun deleteMealLogById(id: Long)

    @Query("SELECT * FROM meal_logs WHERE email = :email AND timestamp >= :timestamp")
    suspend fun getMealLogsSince(email: String, timestamp: Long): List<MealLogEntity>

    // --- Water Log Queries ---
    @Query("SELECT * FROM water_logs WHERE email = :email AND dateLabel = :date ORDER BY timestamp DESC")
    fun observeWaterForDay(email: String, date: String): Flow<List<WaterLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(water: WaterLogEntity)

    @Query("DELETE FROM water_logs WHERE id = :id")
    suspend fun deleteWaterLogById(id: Long)

    @Query("DELETE FROM water_logs WHERE email = :email AND dateLabel = :date")
    suspend fun clearWaterForDay(email: String, date: String)

    @Query("SELECT * FROM water_logs WHERE email = :email AND timestamp >= :timestamp")
    suspend fun getWaterLogsSince(email: String, timestamp: Long): List<WaterLogEntity>

    // --- Weight Log Queries ---
    @Query("SELECT * FROM weight_logs WHERE email = :email ORDER BY dateLabel ASC, timestamp ASC")
    fun observeWeightHistory(email: String): Flow<List<WeightLogEntity>>

    @Query("SELECT * FROM weight_logs WHERE email = :email AND timestamp >= :timestamp")
    suspend fun getWeightLogsSince(email: String, timestamp: Long): List<WeightLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeightLog(weight: WeightLogEntity)

    @Query("DELETE FROM weight_logs WHERE id = :id")
    suspend fun deleteWeightLogById(id: Long)
    // --- Meal Preset Queries ---
    @Query("SELECT * FROM meal_presets WHERE email = :email ORDER BY timestamp DESC")
    fun observeMealPresets(email: String): Flow<List<MealPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPreset(preset: MealPresetEntity)

    @Query("DELETE FROM meal_presets WHERE id = :id")
    suspend fun deleteMealPresetById(id: Long)

    // --- Streak Queries ---
    @Query("SELECT * FROM user_streaks WHERE email = :email LIMIT 1")
    fun observeStreak(email: String): Flow<StreakEntity?>

    @Query("SELECT * FROM user_streaks WHERE email = :email LIMIT 1")
    suspend fun getStreak(email: String): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStreak(streak: StreakEntity)

    // --- Chat Queries ---
    @Query("SELECT * FROM chat_messages WHERE email = :email ORDER BY timestamp ASC")
    fun observeChatMessages(email: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE email = :email")
    suspend fun clearChatHistory(email: String)
    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)
    
    @Query("DELETE FROM meal_logs WHERE email = :email")
    suspend fun deleteAllMealLogs(email: String)

    @Query("DELETE FROM water_logs WHERE email = :email")
    suspend fun deleteAllWaterLogs(email: String)

    @Query("DELETE FROM weight_logs WHERE email = :email")
    suspend fun deleteAllWeightLogs(email: String)
    
    @Query("DELETE FROM meal_presets WHERE email = :email")
    suspend fun deleteAllMealPresets(email: String)

    @Query("DELETE FROM user_streaks WHERE email = :email")
    suspend fun deleteUserStreaks(email: String)
}
