package com.example.data.engine

import com.example.data.model.CalculatedNutrients
import com.example.data.model.RecipeIngredientItem
import com.example.data.repository.FoodCompositionRepository

/**
 * Deterministic Nutrition Calculation Engine.
 * Implements PRD Section 8 & NutriGen principles:
 * Ingredient nutrient = (ingredient quantity in grams / 100) * nutrient value per 100 g
 * Total recipe value = sum of ingredient contributions, adjusted for the number of servings.
 */
object NutritionCalculator {

  data class IngredientNutrientContribution(
    val ingredientId: String,
    val displayName: String,
    val quantityGrams: Float,
    val caloriesKcal: Float,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val fiberG: Float,
    val sugarG: Float,
    val sodiumMg: Float,
    val potassiumMg: Float,
    val calciumMg: Float = 0f,
    val ironMg: Float = 0f,
    val vitaminCMg: Float = 0f,
    val vitaminKMcg: Float = 0f,
    val saturatedFatG: Float = 0f,
    val dataSource: String
  )

  /**
   * Calculates total nutrients from a list of recipe ingredients and total yield in servings.
   */
  fun calculateRecipeNutrients(
    items: List<RecipeIngredientItem>,
    servingsYield: Int = 1
  ): Pair<CalculatedNutrients, List<IngredientNutrientContribution>> {
    val effectiveServings = if (servingsYield > 0) servingsYield.toFloat() else 1.0f

    var totalCalories = 0f
    var totalProtein = 0f
    var totalCarbs = 0f
    var totalFat = 0f
    var totalFiber = 0f
    var totalSugar = 0f
    var totalSodium = 0f
    var totalPotassium = 0f
    var totalCalcium = 0f
    var totalIron = 0f
    var totalVitC = 0f
    var totalVitK = 0f
    var totalSatFat = 0f

    val contributions = mutableListOf<IngredientNutrientContribution>()

    for (item in items) {
      val ingredient = FoodCompositionRepository.getIngredient(item.ingredientId)
      if (ingredient != null) {
        val factor = item.quantityGrams / 100f
        val cal = ingredient.caloriesPer100g * factor
        val pro = ingredient.proteinPer100g * factor
        val carb = ingredient.carbsPer100g * factor
        val fat = ingredient.fatPer100g * factor
        val fib = ingredient.fiberPer100g * factor
        val sug = ingredient.sugarPer100g * factor
        val sod = ingredient.sodiumMgPer100g * factor
        val pot = ingredient.potassiumMgPer100g * factor
        val ca = ingredient.calciumMgPer100g * factor
        val fe = ingredient.ironMgPer100g * factor
        val vc = ingredient.vitaminCMgPer100g * factor
        val vk = ingredient.vitaminKMcgPer100g * factor
        val sat = ingredient.saturatedFatPer100g * factor

        totalCalories += cal
        totalProtein += pro
        totalCarbs += carb
        totalFat += fat
        totalFiber += fib
        totalSugar += sug
        totalSodium += sod
        totalPotassium += pot
        totalCalcium += ca
        totalIron += fe
        totalVitC += vc
        totalVitK += vk
        totalSatFat += sat

        contributions.add(
          IngredientNutrientContribution(
            ingredientId = item.ingredientId,
            displayName = item.displayName,
            quantityGrams = item.quantityGrams,
            caloriesKcal = cal / effectiveServings,
            proteinG = pro / effectiveServings,
            carbsG = carb / effectiveServings,
            fatG = fat / effectiveServings,
            fiberG = fib / effectiveServings,
            sugarG = sug / effectiveServings,
            sodiumMg = sod / effectiveServings,
            potassiumMg = pot / effectiveServings,
            calciumMg = ca / effectiveServings,
            ironMg = fe / effectiveServings,
            vitaminCMg = vc / effectiveServings,
            vitaminKMcg = vk / effectiveServings,
            saturatedFatG = sat / effectiveServings,
            dataSource = ingredient.dataSource
          )
        )
      }
    }

    val perServing = CalculatedNutrients(
      caloriesKcal = totalCalories / effectiveServings,
      proteinGrams = totalProtein / effectiveServings,
      carbsGrams = totalCarbs / effectiveServings,
      fatGrams = totalFat / effectiveServings,
      fiberGrams = totalFiber / effectiveServings,
      sugarGrams = totalSugar / effectiveServings,
      sodiumMg = totalSodium / effectiveServings,
      potassiumMg = totalPotassium / effectiveServings,
      calciumMg = totalCalcium / effectiveServings,
      ironMg = totalIron / effectiveServings,
      vitaminCMg = totalVitC / effectiveServings,
      vitaminKMcg = totalVitK / effectiveServings,
      saturatedFatGrams = totalSatFat / effectiveServings
    )

    return Pair(perServing, contributions)
  }
}
