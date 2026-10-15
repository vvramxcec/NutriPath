package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Meal plan record stored in Room.
 * Can be either "PLANNED" or "CONSUMED".
 * PRD: Planned meals must not be counted as consumed until the user logs them.
 */
@Entity(tableName = "meal_plans")
data class MealPlanEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val date: String, // Format: YYYY-MM-DD
  val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
  val recipeId: String,
  val recipeTitle: String,
  val servings: Float = 1.0f,
  val caloriesKcal: Float,
  val proteinGrams: Float,
  val carbsGrams: Float,
  val fatGrams: Float,
  val fiberGrams: Float,
  val sodiumMg: Float,
  val status: String = STATUS_PLANNED, // "PLANNED" or "CONSUMED"
  val validationStatus: String = "APPROVED",
  val suitabilityExplanation: String = "",
  val ingredientsSummary: String = "",
  val instructionsSummary: String = "",
  val timestamp: Long = System.currentTimeMillis()
) {
  companion object {
    const val STATUS_PLANNED = "PLANNED"
    const val STATUS_CONSUMED = "CONSUMED"
  }
}
