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
  val name: String = "",
  val age: Int = 0,
  val gender: String = "", // "Male", "Female", "Other"
  val weightKg: Float = 0f,
  val heightCm: Float = 0f,
  val activityLevel: String = "Moderately Active", // Sedentary, Lightly Active, Moderately Active, Very Active
  val medicalConditions: List<String> = emptyList(),
  val medications: List<String> = emptyList(),
  val allergies: List<String> = emptyList(),
  val dietaryPreference: String = "Balanced",
  val dietaryRestrictions: List<String> = emptyList(),
  val nutritionGoal: String = "Eat Healthy & Maintain",
  val clinicianNotes: String = "",
  val dailyCalorieTarget: Int = 2000,
  val proteinTargetG: Int = 75,
  val carbsTargetG: Int = 250,
  val fatTargetG: Int = 60,
  val maxSodiumMg: Int = 2300,
  val maxPotassiumMg: Int = 3000,
  val systolicBp: Int = 120,
  val diastolicBp: Int = 80,
  val hba1c: Float = 5.5f,
  val serumPotassium: Float = 4.0f,
  val excludedIngredients: List<String> = emptyList(),
  val preferredIngredients: List<String> = emptyList(),
  val consentGiven: Boolean = true,
  val hasCompletedOnboarding: Boolean = false,
  val lastUpdatedMillis: Long = System.currentTimeMillis()
)

object MedicalConditionCatalog {
  val AVAILABLE_CONDITIONS = listOf(
    "Diabetes Type 2",
    "Hypertension",
    "Thyroid (Hypo/Hyperthyroid)",
    "Arthritis (Rheumatoid/Osteo/Gout)",
    "Chronic Kidney Disease (CKD)",
    "High Cholesterol / Hyperlipidemia",
    "Celiac Disease",
    "GERD / Acid Reflux"
  )

  val CONDITION_DESCRIPTIONS = mapOf(
    "Diabetes Type 2" to "Enforces low glycemic load, restricts simple sugars (< 10g), promotes soluble fiber (Matta rice, oats).",
    "Hypertension" to "Limits daily sodium strictly to 1500mg (DASH protocol) and reduces seasoning salts.",
    "Thyroid (Hypo/Hyperthyroid)" to "Balances iodine, selenium & zinc; moderates unheated goitrogenic crucifers and spaces meals from thyroid hormone medications.",
    "Arthritis (Rheumatoid/Osteo/Gout)" to "Anti-inflammatory dietary protocol rich in omega-3 and antioxidants; strictly limits high-purine foods, trans-fats, and inflammatory sugars.",
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
