package com.example.data.repository

import com.example.data.db.MealPlanDao
import com.example.data.model.MealPlanEntity
import kotlinx.coroutines.flow.Flow

class MealPlanRepository(private val mealPlanDao: MealPlanDao) {

  fun getMealsForDate(date: String): Flow<List<MealPlanEntity>> {
    return mealPlanDao.getMealsForDate(date)
  }

  fun getConsumedMealsForDate(date: String): Flow<List<MealPlanEntity>> {
    return mealPlanDao.getConsumedMealsForDate(date)
  }

  fun getAllConsumedHistory(): Flow<List<MealPlanEntity>> {
    return mealPlanDao.getAllConsumedHistory()
  }

  fun getAllMeals(): Flow<List<MealPlanEntity>> {
    return mealPlanDao.getAllMeals()
  }

  suspend fun getAllMealsOnce(): List<MealPlanEntity> {
    return mealPlanDao.getAllMealsOnce()
  }

  suspend fun getMealById(id: Long): MealPlanEntity? {
    return mealPlanDao.getMealById(id)
  }

  suspend fun insertMeal(meal: MealPlanEntity): Long {
    return mealPlanDao.insertMeal(meal)
  }

  suspend fun insertMeals(meals: List<MealPlanEntity>) {
    mealPlanDao.insertMeals(meals)
  }

  suspend fun markMealConsumed(id: Long) {
    mealPlanDao.markMealConsumed(id)
  }

  suspend fun markMealPlanned(id: Long) {
    mealPlanDao.markMealPlanned(id)
  }

  suspend fun updateServings(id: Long, newServings: Float, singleServingCalories: Float, singleServingProtein: Float, singleServingCarbs: Float, singleServingFat: Float, singleServingSodium: Float) {
    mealPlanDao.updateServingsAndNutrients(
      id = id,
      servings = newServings,
      calories = singleServingCalories * newServings,
      protein = singleServingProtein * newServings,
      carbs = singleServingCarbs * newServings,
      fat = singleServingFat * newServings,
      sodium = singleServingSodium * newServings
    )
  }

  suspend fun deleteMeal(id: Long) {
    mealPlanDao.deleteMealById(id)
  }

  suspend fun getMealsForDateOnce(date: String): List<MealPlanEntity> {
    return mealPlanDao.getMealsForDateOnce(date)
  }

  suspend fun deletePlannedMealsForDate(date: String) {
    mealPlanDao.deletePlannedMealsForDate(date)
  }

  suspend fun clearAll() {
    mealPlanDao.deleteAllMeals()
  }
}
