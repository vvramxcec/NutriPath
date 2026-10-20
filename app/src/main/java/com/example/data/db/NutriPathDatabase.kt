package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.HealthProfile
import com.example.data.model.MealPlanEntity

@Database(
  entities = [HealthProfile::class, MealPlanEntity::class],
  version = 2,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NutriPathDatabase : RoomDatabase() {
  abstract fun healthProfileDao(): HealthProfileDao
  abstract fun mealPlanDao(): MealPlanDao

  companion object {
    @Volatile
    private var INSTANCE: NutriPathDatabase? = null

    fun getDatabase(context: Context): NutriPathDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          NutriPathDatabase::class.java,
          "nutripath_database"
        )
          .fallbackToDestructiveMigration()
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
