package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Health profile representing user health parameters, medical conditions,
 * food allergies, dietary restrictions, and clinical nutrient targets.
 */
@Entity(tableName = "health_profile")
data class HealthProfile(
  @PrimaryKey
  val id: Int = 1,
  val name: String = "Patient / User",
  val age: Int = 36,
  val gender: String = "Male", // "Male", "Female", "Other"
  val weightKg: Float = 72.0f,
  val heightCm: Float = 172.0f,
  val activityLevel: String = "Moderately Active", // Sedentary, Lightly Active, Moderately Active, Very Active
  val medicalConditions: List<String> = listOf("Diabetes Type 2", "Hypertension"),
  val allergies: List<String> = listOf("Peanuts"),
  val dietaryPreference: String = "Kerala / South Indian", // Kerala / South Indian, Indian Balanced, Vegetarian, Vegan, Eggetarian, Non-Vegetarian
  val dietaryRestrictions: List<String> = listOf("Low Sodium (< 1500mg)", "Low Glycemic Index"),
  val nutritionGoal: String = "Blood Sugar Stability & BP Control",
  val clinicianNotes: String = "Avoid high GI rice varieties. Limit table salt. High fiber recommended.",
  val dailyCalorieTarget: Int = 1950,
  val proteinTargetG: Int = 85,
  val carbsTargetG: Int = 220,
  val fatTargetG: Int = 55,
  val maxSodiumMg: Int = 1500,
  val consentGiven: Boolean = true,
  val lastUpdatedMillis: Long = System.currentTimeMillis()
)

object MedicalConditionCatalog {
  val AVAILABLE_CONDITIONS = listOf(
    "Diabetes Type 2",
    "Hypertension",
    "Chronic Kidney Disease (CKD)",
    "High Cholesterol / Hyperlipidemia",
    "Celiac Disease",
    "GERD / Acid Reflux"
  )

  val AVAILABLE_ALLERGIES = listOf(
    "Peanuts",
    "Tree Nuts",
    "Dairy / Lactose",
    "Eggs",
    "Wheat / Gluten",
    "Shellfish",
    "Fish",
    "Soy",
    "Mustard",
    "Sesame"
  )

  val DIETARY_PREFERENCES = listOf(
    "Kerala / South Indian",
    "Indian Balanced",
    "Vegetarian",
    "Vegan",
    "Eggetarian",
    "Non-Vegetarian"
  )

  val ACTIVITY_LEVELS = listOf(
    "Sedentary",
    "Lightly Active",
    "Moderately Active",
    "Very Active"
  )
}
