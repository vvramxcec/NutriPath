package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.HealthProfile
import com.example.data.model.IndianFoodEntity
import com.example.data.model.MealPlanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@Database(
  entities = [HealthProfile::class, MealPlanEntity::class, IndianFoodEntity::class],
  version = 4,
  exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NutriPathDatabase : RoomDatabase() {
  abstract fun healthProfileDao(): HealthProfileDao
  abstract fun mealPlanDao(): MealPlanDao
  abstract fun indianFoodDao(): IndianFoodDao

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

    /**
     * Seeds the 1,015 Indian food items from assets/indian_food_nutrition.csv into SQLite Room DB.
     */
    suspend fun seedIndianFoodDatasetIfEmpty(context: Context, database: NutriPathDatabase) {
      withContext(Dispatchers.IO) {
        try {
          if (database.indianFoodDao().getCount() == 0) {
            val foods = mutableListOf<IndianFoodEntity>()
            context.assets.open("indian_food_nutrition.csv").use { inputStream ->
              val reader = BufferedReader(InputStreamReader(inputStream))
              val header = reader.readLine() // Skip header
              var line = reader.readLine()
              while (line != null) {
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                  // CSV format: Dish Name,Calories,Carbs,Protein,Fats,Free Sugar,Fibre,Sodium,Calcium,Iron,Vitamin C,Folate
                  // Handle possible comma within quotes if any
                  val tokens = parseCsvLine(trimmed)
                  if (tokens.size >= 10) {
                    val dishName = tokens[0]
                    val cal = tokens.getOrNull(1)?.toFloatOrNull() ?: 0f
                    val carbs = tokens.getOrNull(2)?.toFloatOrNull() ?: 0f
                    val pro = tokens.getOrNull(3)?.toFloatOrNull() ?: 0f
                    val fat = tokens.getOrNull(4)?.toFloatOrNull() ?: 0f
                    val sugar = tokens.getOrNull(5)?.toFloatOrNull() ?: 0f
                    val fib = tokens.getOrNull(6)?.toFloatOrNull() ?: 0f
                    val sod = tokens.getOrNull(7)?.toFloatOrNull() ?: 0f
                    val calcuim = tokens.getOrNull(8)?.toFloatOrNull() ?: 0f
                    val iron = tokens.getOrNull(9)?.toFloatOrNull() ?: 0f
                    val vitC = tokens.getOrNull(10)?.toFloatOrNull() ?: 0f
                    val folate = tokens.getOrNull(11)?.toFloatOrNull() ?: 0f

                    foods.add(
                      IndianFoodEntity(
                        dishName = dishName,
                        caloriesKcal = cal,
                        carbsGrams = carbs,
                        proteinGrams = pro,
                        fatGrams = fat,
                        freeSugarGrams = sugar,
                        fiberGrams = fib,
                        sodiumMg = sod,
                        calciumMg = calcuim,
                        ironMg = iron,
                        vitaminCMg = vitC,
                        folateMcg = folate
                      )
                    )
                  }
                }
                line = reader.readLine()
              }
            }
            if (foods.isNotEmpty()) {
              database.indianFoodDao().insertAll(foods)
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    private fun parseCsvLine(line: String): List<String> {
      val result = mutableListOf<String>()
      val sb = StringBuilder()
      var inQuotes = false
      for (ch in line) {
        when (ch) {
          '"' -> inQuotes = !inQuotes
          ',' -> {
            if (inQuotes) {
              sb.append(ch)
            } else {
              result.add(sb.toString().trim())
              sb.setLength(0)
            }
          }
          else -> sb.append(ch)
        }
      }
      result.add(sb.toString().trim())
      return result
    }
  }
}
