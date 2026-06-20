package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        MealLogEntity::class,
        WaterLogEntity::class,
        WeightLogEntity::class,
        MealPresetEntity::class,
        StreakEntity::class,
        ChatMessageEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class NutriDatabase : RoomDatabase() {
    abstract fun nutriDao(): NutriDao

    companion object {
        @Volatile
        private var INSTANCE: NutriDatabase? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `meal_presets` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `presetName` TEXT NOT NULL, `mealType` TEXT NOT NULL, `calories` REAL NOT NULL, `protein` REAL NOT NULL, `carbs` REAL NOT NULL, `fats` REAL NOT NULL, `fiber` REAL NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_streaks` (`email` TEXT NOT NULL, `loginStreak` INTEGER NOT NULL, `proteinStreak` INTEGER NOT NULL, `waterStreak` INTEGER NOT NULL, `weightStreak` INTEGER NOT NULL, `lastLoginDate` TEXT NOT NULL, `lastProteinDate` TEXT NOT NULL, `lastWaterDate` TEXT NOT NULL, `lastWeightDate` TEXT NOT NULL, PRIMARY KEY(`email`))"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `chat_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `role` TEXT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)"
                )
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `users` ADD COLUMN `targetWeightKg` REAL NOT NULL DEFAULT 70.0")
            }
        }

        fun getDatabase(context: Context): NutriDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NutriDatabase::class.java,
                    "nutritrack_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
