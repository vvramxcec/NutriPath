package com.example.data.model

/**
 * Drug-Nutrient Interaction (DNI) safety knowledge base.
 * Derived from FDA DailyMed and clinical guidelines per NutriOrion (arXiv:2602.18650).
 * Prevents the "Healthy Food Trap" where beneficial foods are contraindicated for polypharmacy patients.
 */
data class DrugNutrientRule(
  val id: String,
  val medicationKeyword: String, // e.g., "Warfarin", "Coumadin", "Spironolactone", "Lisinopril", "Atorvastatin"
  val drugClass: String,
  val contraindicatedNutrientOrFood: String,
  val prohibitedKeywords: List<String>,
  val severity: DniSeverity,
  val mechanismRationale: String,
  val saferAlternativeAdvice: String
)

enum class DniSeverity {
  CRITICAL_BLACK_BOX, // Life-threatening / major bleeding or severe hyperkalemia
  HIGH_RISK,          // Significant clinical compromise
  MODERATE_CAUTION    // Monitoring needed
}

object DrugNutrientCatalog {
  val RULES = listOf(
    DrugNutrientRule(
      id = "DNI-WARFARIN-VITK",
      medicationKeyword = "Warfarin",
      drugClass = "Vitamin K Antagonist / Anticoagulant",
      contraindicatedNutrientOrFood = "Vitamin K Rich Foods (> 100mcg/serving)",
      prohibitedKeywords = listOf("spinach", "palak", "kale", "cheera", "amaranth leaves", "broccoli", "brussels sprouts", "methi", "fenugreek leaves", "green cabbage"),
      severity = DniSeverity.CRITICAL_BLACK_BOX,
      mechanismRationale = "High Vitamin K intake counteracts Warfarin anticoagulation by activating clotting factors II, VII, IX, and X, precipitating thrombotic stroke or pulmonary embolism.",
      saferAlternativeAdvice = "Substitute with low-Vitamin K Kerala vegetables: Bottle gourd (lauki), Ash gourd (kumbalanga), Cucumber (vellarikka), Snake gourd (padavalanga), or Green beans."
    ),
    DrugNutrientRule(
      id = "DNI-DIURETIC-POTASSIUM",
      medicationKeyword = "Spironolactone",
      drugClass = "Potassium-Sparing Diuretic / Aldosterone Antagonist",
      contraindicatedNutrientOrFood = "Excess Potassium (> 600mg/serving) & Salt Substitutes",
      prohibitedKeywords = listOf("potassium chloride", "potassium salt", "banana", "avocado", "coconut water", "elaneer", "potato chips", "concentrated tomato paste"),
      severity = DniSeverity.CRITICAL_BLACK_BOX,
      mechanismRationale = "Spironolactone inhibits renal aldosterone, drastically lowering potassium excretion. High potassium dietary load induces life-threatening cardiac arrhythmias (Hyperkalemia).",
      saferAlternativeAdvice = "Limit coconut water and bananas. Opt for moderate-potassium fruits like apples, papaya, or guava, and season with natural herbs instead of potassium salt."
    ),
    DrugNutrientRule(
      id = "DNI-ACEI-POTASSIUM",
      medicationKeyword = "Lisinopril",
      drugClass = "ACE Inhibitor / Angiotensin Receptor Blocker",
      contraindicatedNutrientOrFood = "High Potassium & Potassium-Based Table Salts",
      prohibitedKeywords = listOf("potassium salt", "salt substitute", "nu-salt", "coconut water"),
      severity = DniSeverity.HIGH_RISK,
      mechanismRationale = "ACE inhibitors reduce aldosterone production, leading to decreased urinary potassium excretion and potential hyperkalemia when combined with high-potassium supplements or substitutes.",
      saferAlternativeAdvice = "Avoid 'low-sodium' salt substitutes containing potassium chloride. Use lemon juice, roasted cumin, and curry leaves for flavor."
    ),
    DrugNutrientRule(
      id = "DNI-STATIN-GRAPEFRUIT",
      medicationKeyword = "Atorvastatin",
      drugClass = "HMG-CoA Reductase Inhibitor (Statin)",
      contraindicatedNutrientOrFood = "Grapefruit & Seville Oranges",
      prohibitedKeywords = listOf("grapefruit", "pomelo", "seville orange"),
      severity = DniSeverity.HIGH_RISK,
      mechanismRationale = "Furanocoumarins in grapefruit irreversibly inhibit intestinal CYP3A4, increasing bioavailability and serum statin levels by up to 300%, triggering rhabdomyolysis and liver toxicity.",
      saferAlternativeAdvice = "Replace grapefruit with oranges, sweet limes (mosambi), lemons, or Indian gooseberry (amla)."
    ),
    DrugNutrientRule(
      id = "DNI-METFORMIN-GLYCEMIC",
      medicationKeyword = "Metformin",
      drugClass = "Biguanide Antidiabetic",
      contraindicatedNutrientOrFood = "Simple Sugars & High GI Meals",
      prohibitedKeywords = listOf("refined sugar", "jaggery", "cane sugar", "white refined flour", "maida"),
      severity = DniSeverity.MODERATE_CAUTION,
      mechanismRationale = "Refined high-glycemic foods overwhelm metformin's hepatic gluconeogenesis suppression, driving postprandial glucose surges and insulin spikes.",
      saferAlternativeAdvice = "Pair whole grains (Kerala Matta red rice, whole ragi) with high soluble fiber and pulse proteins."
    )
  )

  /**
   * Evaluates active patient medications against recipe ingredients.
   * Returns matching DNI violations.
   */
  fun evaluateContraindications(
    medications: List<String>,
    recipeIngredients: List<RecipeIngredientItem>
  ): List<DrugNutrientRule> {
    val violations = mutableListOf<DrugNutrientRule>()
    for (med in medications) {
      val matchingRule = RULES.firstOrNull { rule ->
        med.contains(rule.medicationKeyword, ignoreCase = true)
      } ?: continue

      val hasTrigger = recipeIngredients.any { ing ->
        val nameLower = ing.displayName.lowercase()
        matchingRule.prohibitedKeywords.any { keyword -> nameLower.contains(keyword) }
      }

      if (hasTrigger) {
        violations.add(matchingRule)
      }
    }
    return violations
  }
}
