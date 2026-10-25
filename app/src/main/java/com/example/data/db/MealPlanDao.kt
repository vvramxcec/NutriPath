package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MealPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
  @Query("SELECT * FROM meal_plans WHERE date = :date ORDER BY id ASC")
  fun getMealsForDate(date: String): Flow<List<MealPlanEntity>>

  @Query("SELECT * FROM meal_plans WHERE date = :date AND status = 'CONSUMED'")
  fun getConsumedMealsForDate(date: String): Flow<List<MealPlanEntity>>

  @Query("SELECT * FROM meal_plans WHERE status = 'CONSUMED' ORDER BY timestamp DESC")
  fun getAllConsumedHistory(): Flow<List<MealPlanEntity>>

  @Query("SELECT * FROM meal_plans ORDER BY date DESC, id DESC")
  fun getAllMeals(): Flow<List<MealPlanEntity>>

  @Query("SELECT * FROM meal_plans ORDER BY date DESC, id DESC")
  suspend fun getAllMealsOnce(): List<MealPlanEntity>

  @Query("SELECT * FROM meal_plans WHERE id = :id LIMIT 1")
  suspend fun getMealById(id: Long): MealPlanEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMeal(meal: MealPlanEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMeals(meals: List<MealPlanEntity>)

  @Update
  suspend fun updateMeal(meal: MealPlanEntity)

  @Query("UPDATE meal_plans SET status = 'CONSUMED', timestamp = :timestamp WHERE id = :id")
  suspend fun markMealConsumed(id: Long, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE meal_plans SET status = 'PLANNED' WHERE id = :id")
  suspend fun markMealPlanned(id: Long)

  @Query("UPDATE meal_plans SET servings = :servings, caloriesKcal = :calories, proteinGrams = :protein, carbsGrams = :carbs, fatGrams = :fat, sodiumMg = :sodium WHERE id = :id")
  suspend fun updateServingsAndNutrients(id: Long, servings: Float, calories: Float, protein: Float, carbs: Float, fat: Float, sodium: Float)

  @Query("SELECT * FROM meal_plans WHERE date = :date ORDER BY id ASC")
  suspend fun getMealsForDateOnce(date: String): List<MealPlanEntity>

  @Query("DELETE FROM meal_plans WHERE date = :date AND status = 'PLANNED'")
  suspend fun deletePlannedMealsForDate(date: String)

  @Query("DELETE FROM meal_plans WHERE id = :id")
  suspend fun deleteMealById(id: Long)

  @Query("DELETE FROM meal_plans WHERE date = :date AND mealType = :mealType")
  suspend fun deleteMealForDateAndType(date: String, mealType: String)

  @Query("DELETE FROM meal_plans")
  suspend fun deleteAllMeals()
}
