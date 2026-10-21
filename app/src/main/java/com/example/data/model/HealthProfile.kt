package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Health profile representing user health parameters, medical conditions,
 * medications, food allergies, dietary restrictions, clinical nutrient targets, and onboarding state.
 */
@Entity(tableName = "health_profile")
data class HealthProfile(
  @PrimaryKey
  val id: Int = 1,
  val name: String = "Patient / User",
  val age: Int = 42,
  val gender: String = "Male", // "Male", "Female", "Other"
  val weightKg: Float = 72.0f,
  val heightCm: Float = 172.0f,
  val activityLevel: String = "Moderately Active", // Sedentary, Lightly Active, Moderately Active, Very Active
  val medicalConditions: List<String> = listOf("Diabetes Type 2", "Hypertension"),
  val medications: List<String> = listOf("Metformin"), // e.g. Warfarin, Spironolactone, Lisinopril, Atorvastatin, Metformin
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
  val maxPotassiumMg: Int = 2800, // Important for CKD and K-Sparing Diuretics
  val systolicBp: Int = 135,
  val diastolicBp: Int = 85,
  val hba1c: Float = 6.8f,
  val serumPotassium: Float = 4.2f, // mEq/L normal range 3.5 - 5.0
  val excludedIngredients: List<String> = emptyList(), // HealthGenie circular interactive excluded items
  val preferredIngredients: List<String> = emptyList(), // HealthGenie circular preferred items
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

  val COMMON_MEDICATIONS = listOf(
    "None",
    "Metformin (Anti-diabetic)",
    "Warfarin / Coumadin (Anticoagulant)",
    "Lisinopril / ACE Inhibitor (Antihypertensive)",
    "Spironolactone / Diuretic (Potassium-Sparing)",
    "Atorvastatin / Statin (Cholesterol)",
    "Amlodipine (Calcium Channel Blocker)",
    "Levothyroxine (Thyroid)"
  )

  val MEDICATION_WARNINGS = mapOf(
    "Warfarin / Coumadin (Anticoagulant)" to "Critical interaction with Vitamin K: High-intake greens (Spinach/Palak, Kale, Cheera) counteract medication and increase blood clot risk.",
    "Spironolactone / Diuretic (Potassium-Sparing)" to "Hyperkalemia hazard: Restrict high-potassium foods (potassium salt substitutes, bananas, coconut water).",
    "Lisinopril / ACE Inhibitor (Antihypertensive)" to "Risk of elevated potassium: Avoid potassium-based salt substitutes.",
    "Atorvastatin / Statin (Cholesterol)" to "Severe interaction with Grapefruit / pomelo: Blocks enzyme CYP3A4, causing dangerous medication accumulation."
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
