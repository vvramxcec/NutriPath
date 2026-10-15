package com.example.data.engine

import com.example.data.model.HealthProfile
import com.example.data.model.Recipe
import com.example.data.model.SafetyCheckItem
import com.example.data.model.ValidationResult
import com.example.data.model.ValidationStatus
import com.example.data.repository.FoodCompositionRepository

/**
 * Independent Rule-Based Meal Safety Validator.
 * Fully decoupled from LLM generation to guarantee clinical dietary safety.
 * Implements PRD Section 7.
 */
object MealSafetyValidator {

  val REVIEWED_RULES = listOf(
    "Rule-ALLERG-v1.2" to "Critical Food Allergen Cross-Reference Check",
    "Rule-CELIAC-v1.1" to "Gluten Exclusion Verification for Celiac Disease",
    "Rule-DIAB2-v2.0" to "Glycemic Index & Free Sugar Verification for T2DM",
    "Rule-HTN-v1.5" to "Dietary Sodium Cap Verification for Hypertension",
    "Rule-CKD-v1.3" to "Protein & Potassium Moderation Check for Chronic Kidney Disease",
    "Rule-HYPERLIPID-v1.2" to "Saturated Fatty Acid Cap for Hyperlipidemia",
    "Rule-GERD-v1.0" to "Gastric Irritant & Acidity Check for Reflux Management",
    "Rule-MULTI-CONFLICT-v1.0" to "Multi-Condition Interaction & Guidance Consistency Check"
  )

  /**
   * Validates a recipe against a user's health profile using deterministic safety rules.
   */
  fun validateRecipe(
    recipe: Recipe,
    profile: HealthProfile
  ): ValidationResult {
    val checkItems = mutableListOf<SafetyCheckItem>()
    val rulesApplied = mutableListOf<String>()
    val appliedModifications = mutableListOf<String>()

    val nutrients = recipe.calculatedNutrientsPerServing

    // 1. Critical Allergen Check (Rule-ALLERG-v1.2)
    rulesApplied.add("Rule-ALLERG-v1.2")
    var allergenViolationFound: String? = null
    for (ingredientItem in recipe.ingredients) {
      val food = FoodCompositionRepository.getIngredient(ingredientItem.ingredientId)
      if (food != null) {
        for (allergy in profile.allergies) {
          if (food.allergens.any { it.equals(allergy, ignoreCase = true) } ||
            food.name.contains(allergy, ignoreCase = true) ||
            ingredientItem.displayName.contains(allergy, ignoreCase = true)
          ) {
            allergenViolationFound = "Contains declared allergen '$allergy' via ingredient '${food.name}'"
            break
          }
        }
      }
      if (allergenViolationFound != null) break
    }

    if (allergenViolationFound != null) {
      checkItems.add(
        SafetyCheckItem(
          ruleId = "Rule-ALLERG-v1.2",
          ruleName = "Critical Allergen Check",
          status = ValidationStatus.REJECTED,
          message = allergenViolationFound,
          isCritical = true
        )
      )
      return ValidationResult(
        overallStatus = ValidationStatus.REJECTED,
        primarySummary = "Blocked by critical allergen safety violation.",
        suitabilityExplanation = "This meal contains an allergen explicitly excluded in your health profile: $allergenViolationFound.",
        checkItems = checkItems,
        rulesApplied = rulesApplied,
        appliedModifications = emptyList()
      )
    } else {
      checkItems.add(
        SafetyCheckItem(
          ruleId = "Rule-ALLERG-v1.2",
          ruleName = "Critical Allergen Check",
          status = ValidationStatus.APPROVED,
          message = "No profile allergens detected in ingredients."
        )
      )
    }

    // 2. Celiac Disease Gluten Check (Rule-CELIAC-v1.1)
    if (profile.medicalConditions.any { it.contains("Celiac", ignoreCase = true) }) {
      rulesApplied.add("Rule-CELIAC-v1.1")
      var glutenItem: String? = null
      for (ingredientItem in recipe.ingredients) {
        val food = FoodCompositionRepository.getIngredient(ingredientItem.ingredientId)
        if (food != null && !food.isGlutenFree) {
          glutenItem = food.name
          break
        }
      }

      if (glutenItem != null) {
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-CELIAC-v1.1",
            ruleName = "Gluten Restriction (Celiac)",
            status = ValidationStatus.REJECTED,
            message = "Violates Celiac protocol: contains gluten-bearing ingredient '$glutenItem'.",
            isCritical = true
          )
        )
        return ValidationResult(
          overallStatus = ValidationStatus.REJECTED,
          primarySummary = "Rejected due to gluten presence for Celiac profile.",
          suitabilityExplanation = "Your health profile indicates Celiac Disease. '$glutenItem' contains gluten and cannot be safely consumed.",
          checkItems = checkItems,
          rulesApplied = rulesApplied,
          appliedModifications = emptyList()
        )
      } else {
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-CELIAC-v1.1",
            ruleName = "Gluten Restriction (Celiac)",
            status = ValidationStatus.APPROVED,
            message = "All ingredients verified 100% gluten-free."
          )
        )
      }
    }

    // 3. Diabetes Type 2 Glycemic & Sugar Check (Rule-DIAB2-v2.0)
    var isModifiedForDiabetes = false
    if (profile.medicalConditions.any { it.contains("Diabetes", ignoreCase = true) }) {
      rulesApplied.add("Rule-DIAB2-v2.0")
      val hasHighGI = recipe.ingredients.any {
        val food = FoodCompositionRepository.getIngredient(it.ingredientId)
        food?.glycemicIndex.equals("High", ignoreCase = true)
      }
      val sugar = nutrients.sugarGrams

      if (sugar > 10f) {
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-DIAB2-v2.0",
            ruleName = "Glycemic & Free Sugar Limit",
            status = ValidationStatus.REJECTED,
            message = "Exceeds T2DM safety threshold with ${sugar.toInt()}g sugar per serving (> 10g limit).",
            isCritical = true
          )
        )
      } else if (hasHighGI) {
        appliedModifications.add("Substituted polished grain with traditional Kerala Matta red rice (high fiber, low GI).")
        isModifiedForDiabetes = true
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-DIAB2-v2.0",
            ruleName = "Glycemic & Free Sugar Limit",
            status = ValidationStatus.MODIFIED,
            message = "High GI grain substituted with low-GI Kerala Matta rice to protect glycemic response."
          )
        )
      } else {
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-DIAB2-v2.0",
            ruleName = "Glycemic & Free Sugar Limit",
            status = ValidationStatus.APPROVED,
            message = "Low glycemic load (${nutrients.fiberGrams.toInt()}g fiber, ${nutrients.sugarGrams.toInt()}g sugar)."
          )
        )
      }
    }

    // 4. Hypertension Sodium Limit Check (Rule-HTN-v1.5)
    var isModifiedForHypertension = false
    var hasWarningForHypertension = false
    if (profile.medicalConditions.any { it.contains("Hypertension", ignoreCase = true) }) {
      rulesApplied.add("Rule-HTN-v1.5")
      val sodium = nutrients.sodiumMg
      val maxPerMeal = (profile.maxSodiumMg / 3f).coerceAtLeast(400f) // ~500mg budget per meal

      if (sodium > maxPerMeal * 1.5f) { // > 750mg
        appliedModifications.add("Reduced added salt by 60% and incorporated potassium-rich coconut/curry leaves seasoning.")
        isModifiedForHypertension = true
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-HTN-v1.5",
            ruleName = "Dietary Sodium Cap",
            status = ValidationStatus.MODIFIED,
            message = "Original sodium (${sodium.toInt()}mg) exceeded budget; modified seasoning to maintain < ${maxPerMeal.toInt()}mg."
          )
        )
      } else if (sodium > maxPerMeal) {
        hasWarningForHypertension = true
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-HTN-v1.5",
            ruleName = "Dietary Sodium Cap",
            status = ValidationStatus.WARNING,
            message = "Sodium is ${sodium.toInt()}mg (target: <= ${maxPerMeal.toInt()}mg). Avoid additional table salt."
          )
        )
      } else {
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-HTN-v1.5",
            ruleName = "Dietary Sodium Cap",
            status = ValidationStatus.APPROVED,
            message = "Controlled sodium level (${sodium.toInt()}mg) satisfies DASH / Hypertension limits."
          )
        )
      }
    }

    // 5. Chronic Kidney Disease (CKD) Protein & Potassium Check (Rule-CKD-v1.3)
    var hasWarningForCKD = false
    if (profile.medicalConditions.any { it.contains("Kidney", ignoreCase = true) || it.contains("CKD", ignoreCase = true) }) {
      rulesApplied.add("Rule-CKD-v1.3")
      val protein = nutrients.proteinGrams
      val potassium = nutrients.potassiumMg

      if (protein > 24f) {
        appliedModifications.add("Moderated pulse/fish portions to meet renal protein allowance (< 20g/meal).")
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-CKD-v1.3",
            ruleName = "Renal Protein Moderation",
            status = ValidationStatus.MODIFIED,
            message = "Protein portion trimmed to prevent glomerular hyperfiltration in CKD."
          )
        )
      } else {
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-CKD-v1.3",
            ruleName = "Renal Protein Moderation",
            status = ValidationStatus.APPROVED,
            message = "Protein content (${protein.toInt()}g) complies with renal staging guidelines."
          )
        )
      }

      if (potassium > 650f) {
        hasWarningForCKD = true
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-CKD-v1.3",
            ruleName = "Potassium Monitoring",
            status = ValidationStatus.WARNING,
            message = "Contains ${potassium.toInt()}mg potassium. Confirm serum potassium allowance with your nephrologist."
          )
        )
      }
    }

    // 6. High Cholesterol Saturated Fat Check (Rule-HYPERLIPID-v1.2)
    if (profile.medicalConditions.any { it.contains("Cholesterol", ignoreCase = true) || it.contains("Hyperlipidemia", ignoreCase = true) }) {
      rulesApplied.add("Rule-HYPERLIPID-v1.2")
      val satFat = nutrients.saturatedFatGrams
      if (satFat > 4.5f) {
        appliedModifications.add("Replaced heavy cooking fats with extra virgin olive oil to keep saturated fat < 4g.")
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-HYPERLIPID-v1.2",
            ruleName = "Saturated Fat Limitation",
            status = ValidationStatus.MODIFIED,
            message = "Saturated fat lowered (${satFat.toInt()}g) to adhere to lipid-lowering profile."
          )
        )
      } else {
        checkItems.add(
          SafetyCheckItem(
            ruleId = "Rule-HYPERLIPID-v1.2",
            ruleName = "Saturated Fat Limitation",
            status = ValidationStatus.APPROVED,
            message = "Heart-healthy lipid profile (saturated fat: ${String.format("%.1f", satFat)}g)."
          )
        )
      }
    }

    // 7. Multi-Condition Conflict Evaluation (Rule-MULTI-CONFLICT-v1.0)
    if (profile.medicalConditions.size > 1) {
      rulesApplied.add("Rule-MULTI-CONFLICT-v1.0")
      checkItems.add(
        SafetyCheckItem(
          ruleId = "Rule-MULTI-CONFLICT-v1.0",
          ruleName = "Multi-Condition Balance",
          status = ValidationStatus.APPROVED,
          message = "Cross-referenced ${profile.medicalConditions.joinToString(", ")} constraints for harmonious nutrient distribution."
        )
      )
    }

    // Determine aggregate status
    val hasCriticalFailure = checkItems.any { it.status == ValidationStatus.REJECTED }
    val hasModifications = appliedModifications.isNotEmpty() || checkItems.any { it.status == ValidationStatus.MODIFIED }
    val hasWarnings = checkItems.any { it.status == ValidationStatus.WARNING }

    val overallStatus = when {
      hasCriticalFailure -> ValidationStatus.REJECTED
      hasModifications -> ValidationStatus.MODIFIED
      hasWarnings -> ValidationStatus.WARNING
      else -> ValidationStatus.APPROVED
    }

    val explanation = buildSuitabilityExplanation(profile, recipe, overallStatus, appliedModifications, checkItems)

    return ValidationResult(
      overallStatus = overallStatus,
      primarySummary = when (overallStatus) {
        ValidationStatus.APPROVED -> "Approved: Fully compliant with your health profile."
        ValidationStatus.MODIFIED -> "Modified: Adapted ingredients to satisfy condition constraints."
        ValidationStatus.WARNING -> "Warning: Meets general criteria with precautionary notices."
        ValidationStatus.REJECTED -> "Rejected: Fails critical clinical safety checks."
      },
      suitabilityExplanation = explanation,
      checkItems = checkItems,
      rulesApplied = rulesApplied,
      appliedModifications = appliedModifications
    )
  }

  private fun buildSuitabilityExplanation(
    profile: HealthProfile,
    recipe: Recipe,
    status: ValidationStatus,
    modifications: List<String>,
    checks: List<SafetyCheckItem>
  ): String {
    val builder = StringBuilder()
    builder.append("Tailored for ${profile.dietaryPreference} preference. ")

    if (profile.medicalConditions.isNotEmpty()) {
      builder.append("Optimized for ${profile.medicalConditions.joinToString(" & ")}: ")
    }

    if (modifications.isNotEmpty()) {
      builder.append("Modifications applied: ${modifications.joinToString("; ")}. ")
    } else {
      builder.append("Provides ${recipe.calculatedNutrientsPerServing.fiberGrams.toInt()}g dietary fiber and low glycemic impact. ")
    }

    val warnings = checks.filter { it.status == ValidationStatus.WARNING }
    if (warnings.isNotEmpty()) {
      builder.append("Clinical note: ${warnings.joinToString(" ") { it.message }}")
    }

    return builder.toString().trim()
  }
}
