package com.example

import com.example.data.engine.NutritionCalculator
import com.example.data.model.RecipeIngredientItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionCalculatorTest {

  @Test
  fun testDeterministicNutrientCalculation() {
    // Matta rice cooked: 118 kcal per 100g. 150g = 1.5 * 118 = 177 kcal.
    val items = listOf(
      RecipeIngredientItem("matta_rice_cooked", "Matta Rice", 150f)
    )

    val (nutrients, contributions) = NutritionCalculator.calculateRecipeNutrients(items, servingsYield = 1)

    assertEquals(177f, nutrients.caloriesKcal, 0.5f)
    assertEquals(1, contributions.size)
    assertEquals(177f, contributions[0].caloriesKcal, 0.5f)
    assertTrue(contributions[0].dataSource.contains("ICMR-NIN"))
  }

  @Test
  fun testServingYieldScaling() {
    // 200g cooked moong dal (105 kcal / 100g) = 210 kcal total.
    // Yield: 2 servings -> 105 kcal per serving.
    val items = listOf(
      RecipeIngredientItem("moong_dal_cooked", "Moong Dal", 200f)
    )

    val (nutrients, _) = NutritionCalculator.calculateRecipeNutrients(items, servingsYield = 2)

    assertEquals(105f, nutrients.caloriesKcal, 0.5f)
  }
}
