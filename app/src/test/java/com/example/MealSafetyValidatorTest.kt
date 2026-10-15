package com.example

import com.example.data.engine.MealSafetyValidator
import com.example.data.engine.NutritionCalculator
import com.example.data.model.HealthProfile
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredientItem
import com.example.data.model.ValidationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MealSafetyValidatorTest {

  @Test
  fun testCriticalAllergenRejection() {
    val profile = HealthProfile(
      allergies = listOf("Peanuts")
    )

    val peanutSnack = Recipe(
      id = "test_peanut_snack",
      title = "Peanut Crunch",
      description = "Snack containing peanuts",
      mealType = "Snack",
      defaultServings = 1,
      ingredients = listOf(
        RecipeIngredientItem("peanuts_roasted", "Roasted Peanuts", 30f)
      ),
      preparationSteps = listOf("Roast and serve"),
      calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
        listOf(RecipeIngredientItem("peanuts_roasted", "Roasted Peanuts", 30f))
      ).first
    )

    val result = MealSafetyValidator.validateRecipe(peanutSnack, profile)

    assertEquals(ValidationStatus.REJECTED, result.overallStatus)
    assertTrue(result.checkItems.any { it.isCritical && it.status == ValidationStatus.REJECTED })
  }

  @Test
  fun testCeliacGlutenExclusion() {
    val profile = HealthProfile(
      medicalConditions = listOf("Celiac Disease")
    )

    val wheatRotiRecipe = Recipe(
      id = "test_wheat_roti",
      title = "Atta Roti",
      description = "Whole wheat flatbread",
      mealType = "Dinner",
      defaultServings = 1,
      ingredients = listOf(
        RecipeIngredientItem("atta_whole_wheat_roti", "Whole Wheat Roti", 70f)
      ),
      preparationSteps = listOf("Cook on tava"),
      calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
        listOf(RecipeIngredientItem("atta_whole_wheat_roti", "Whole Wheat Roti", 70f))
      ).first
    )

    val result = MealSafetyValidator.validateRecipe(wheatRotiRecipe, profile)

    assertEquals(ValidationStatus.REJECTED, result.overallStatus)
    assertTrue(result.primarySummary.contains("Rejected", ignoreCase = true))
  }

  @Test
  fun testSafeKeralaMealApproval() {
    val profile = HealthProfile(
      medicalConditions = listOf("Hypertension"),
      allergies = emptyList()
    )

    val safeMattaMeal = Recipe(
      id = "test_safe_matta",
      title = "Kerala Matta Rice with Steamed Fish",
      description = "Low sodium fresh meal",
      mealType = "Lunch",
      defaultServings = 1,
      ingredients = listOf(
        RecipeIngredientItem("matta_rice_cooked", "Matta Rice", 150f),
        RecipeIngredientItem("seer_fish_neymeen", "Seer Fish", 100f),
        RecipeIngredientItem("cabbage_raw", "Cabbage", 80f)
      ),
      preparationSteps = listOf("Steam and serve"),
      calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
        listOf(
          RecipeIngredientItem("matta_rice_cooked", "Matta Rice", 150f),
          RecipeIngredientItem("seer_fish_neymeen", "Seer Fish", 100f),
          RecipeIngredientItem("cabbage_raw", "Cabbage", 80f)
        )
      ).first
    )

    val result = MealSafetyValidator.validateRecipe(safeMattaMeal, profile)

    assertTrue(
      result.overallStatus == ValidationStatus.APPROVED ||
        result.overallStatus == ValidationStatus.MODIFIED
    )
  }
}
