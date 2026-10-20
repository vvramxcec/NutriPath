package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Health profile representing user health parameters, medical conditions,
 * food allergies, dietary restrictions, clinical nutrient targets, and onboarding state.
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
  val hasCompletedOnboarding: Boolean = false,
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

  val CONDITION_DESCRIPTIONS = mapOf(
    "Diabetes Type 2" to "Enforces low glycemic load, restricts simple sugars (< 10g), promotes soluble fiber (Matta rice, oats).",
    "Hypertension" to "Limits daily sodium strictly to 1500mg (DASH protocol) and reduces seasoning salts.",
    "Chronic Kidney Disease (CKD)" to "Moderates protein allowance (< 20g/meal) and monitors potassium/phosphorus loads.",
    "High Cholesterol / Hyperlipidemia" to "Restricts saturated fatty acids (< 4.5g/serving) and prioritizes monounsaturated olive fats.",
    "Celiac Disease" to "Strict 100% zero-gluten protocol; eliminates all wheat, barley, and semolina ingredients.",
    "GERD / Acid Reflux" to "Eliminates high-acidity raw tomatoes and excessive fiery culinary irritants."
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

  val NUTRITION_GOALS = listOf(
    "Blood Sugar Stability & BP Control",
    "Weight Management & Calorie Deficit",
    "Heart Health & Cholesterol Reduction",
    "Renal Protection & Moderate Protein",
    "General Vitality & Clean Eating"
  )
}
