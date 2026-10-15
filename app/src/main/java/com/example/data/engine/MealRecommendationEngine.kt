package com.example.data.engine

import com.example.data.model.HealthProfile
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredientItem
import com.example.data.model.ValidationResult
import com.example.data.model.ValidationStatus
import java.util.Calendar

/**
 * Meal Recommendation Engine.
 * Supports time-aware suggestions, custom meal requests, and full-day meal planning.
 * Every recipe candidate is strictly passed through NutritionCalculator and MealSafetyValidator.
 */
object MealRecommendationEngine {

  data class RecommendedMealCandidate(
    val recipe: Recipe,
    val validationResult: ValidationResult,
    val isSuggestedForCurrentTime: Boolean = false
  )

  data class FullDayMealPlan(
    val date: String,
    val breakfast: RecommendedMealCandidate,
    val lunch: RecommendedMealCandidate,
    val dinner: RecommendedMealCandidate,
    val snack: RecommendedMealCandidate,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val totalFiber: Float,
    val totalSodium: Float,
    val planSuitabilityNote: String
  )

  /**
   * Returns suggested meal type based on current local hour.
   * PRD 4.4: Time-aware meal generation.
   */
  fun getSuggestedMealTypeForCurrentTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
      in 5..10 -> "Breakfast"
      in 11..15 -> "Lunch"
      in 16..18 -> "Snack"
      in 19..22 -> "Dinner"
      else -> "Breakfast" // Late night / early dawn -> plan breakfast
    }
  }

  /**
   * Generates recommendations for a specific meal type tailored to the user profile.
   */
  fun generateMealsForType(
    mealType: String,
    profile: HealthProfile,
    remainingCalories: Int = 500
  ): List<RecommendedMealCandidate> {
    val baseCandidates = getPredefinedRecipeCandidates(mealType, profile)

    return baseCandidates.map { candidateRecipe ->
      // 1. Calculate exact nutrients from ingredient gram weights
      val (nutrients, _) = NutritionCalculator.calculateRecipeNutrients(
        candidateRecipe.ingredients,
        candidateRecipe.defaultServings
      )
      val verifiedRecipe = candidateRecipe.copy(calculatedNutrientsPerServing = nutrients)

      // 2. Validate using independent rule-based safety validator
      val validation = MealSafetyValidator.validateRecipe(verifiedRecipe, profile)

      RecommendedMealCandidate(
        recipe = verifiedRecipe,
        validationResult = validation,
        isSuggestedForCurrentTime = (mealType.equals(getSuggestedMealTypeForCurrentTime(), ignoreCase = true))
      )
    }.sortedBy { candidate ->
      // Place APPROVED and MODIFIED first, WARNING second, REJECTED last
      when (candidate.validationResult.overallStatus) {
        ValidationStatus.APPROVED -> 0
        ValidationStatus.MODIFIED -> 1
        ValidationStatus.WARNING -> 2
        ValidationStatus.REJECTED -> 3
      }
    }
  }

  /**
   * Generates a balanced Full-Day Plan (Breakfast, Lunch, Dinner, Snack).
   */
  fun generateFullDayPlan(
    date: String,
    profile: HealthProfile
  ): FullDayMealPlan {
    val bList = generateMealsForType("Breakfast", profile)
    val lList = generateMealsForType("Lunch", profile)
    val dList = generateMealsForType("Dinner", profile)
    val sList = generateMealsForType("Snack", profile)

    val bestB = bList.firstOrNull { it.validationResult.overallStatus != ValidationStatus.REJECTED } ?: bList.first()
    val bestL = lList.firstOrNull { it.validationResult.overallStatus != ValidationStatus.REJECTED } ?: lList.first()
    val bestD = dList.firstOrNull { it.validationResult.overallStatus != ValidationStatus.REJECTED } ?: dList.first()
    val bestS = sList.firstOrNull { it.validationResult.overallStatus != ValidationStatus.REJECTED } ?: sList.first()

    val totalCal = bestB.recipe.calculatedNutrientsPerServing.caloriesKcal +
      bestL.recipe.calculatedNutrientsPerServing.caloriesKcal +
      bestD.recipe.calculatedNutrientsPerServing.caloriesKcal +
      bestS.recipe.calculatedNutrientsPerServing.caloriesKcal

    val totalPro = bestB.recipe.calculatedNutrientsPerServing.proteinGrams +
      bestL.recipe.calculatedNutrientsPerServing.proteinGrams +
      bestD.recipe.calculatedNutrientsPerServing.proteinGrams +
      bestS.recipe.calculatedNutrientsPerServing.proteinGrams

    val totalCarb = bestB.recipe.calculatedNutrientsPerServing.carbsGrams +
      bestL.recipe.calculatedNutrientsPerServing.carbsGrams +
      bestD.recipe.calculatedNutrientsPerServing.carbsGrams +
      bestS.recipe.calculatedNutrientsPerServing.carbsGrams

    val totalFat = bestB.recipe.calculatedNutrientsPerServing.fatGrams +
      bestL.recipe.calculatedNutrientsPerServing.fatGrams +
      bestD.recipe.calculatedNutrientsPerServing.fatGrams +
      bestS.recipe.calculatedNutrientsPerServing.fatGrams

    val totalFiber = bestB.recipe.calculatedNutrientsPerServing.fiberGrams +
      bestL.recipe.calculatedNutrientsPerServing.fiberGrams +
      bestD.recipe.calculatedNutrientsPerServing.fiberGrams +
      bestS.recipe.calculatedNutrientsPerServing.fiberGrams

    val totalSodium = bestB.recipe.calculatedNutrientsPerServing.sodiumMg +
      bestL.recipe.calculatedNutrientsPerServing.sodiumMg +
      bestD.recipe.calculatedNutrientsPerServing.sodiumMg +
      bestS.recipe.calculatedNutrientsPerServing.sodiumMg

    val note = "Balanced daily plan totaling ${totalCal.toInt()} kcal (${(totalCal / profile.dailyCalorieTarget * 100).toInt()}% of daily target). Includes high-fiber Kerala staples with verified sodium and glycemic thresholds."

    return FullDayMealPlan(
      date = date,
      breakfast = bestB,
      lunch = bestL,
      dinner = bestD,
      snack = bestS,
      totalCalories = totalCal,
      totalProtein = totalPro,
      totalCarbs = totalCarb,
      totalFat = totalFat,
      totalFiber = totalFiber,
      totalSodium = totalSodium,
      planSuitabilityNote = note
    )
  }

  private fun getPredefinedRecipeCandidates(mealType: String, profile: HealthProfile): List<Recipe> {
    return when (mealType.lowercase()) {
      "breakfast" -> listOf(
        Recipe(
          id = "rec_brk_idli_sambar",
          title = "Steamed Idli with Drumstick Sambar & Mint Chutney",
          description = "Traditional fermented rice-lentil steamed cakes paired with nutrient-dense vegetable sambar.",
          mealType = "Breakfast",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("steamed_idli_rice_urad", "Steamed Idli (2 pcs)", 120f, "120g"),
            RecipeIngredientItem("toor_dal_cooked", "Toor Dal Sambar Lentils", 100f, "100g"),
            RecipeIngredientItem("drumstick_moringa", "Drumstick Pieces in Sambar", 50f, "50g"),
            RecipeIngredientItem("tomato_fresh", "Tomato for Sambar Tang", 40f, "40g"),
            RecipeIngredientItem("table_salt", "Iodized Salt (measured)", 0.6f, "0.6g")
          ),
          preparationSteps = listOf(
            "Steam fermented batter in idli cooker for 10 minutes until soft and fluffy.",
            "Simmer cooked toor dal with fresh drumstick and chopped tomatoes with a pinch of turmeric.",
            "Season with mustard seeds and curry leaves in minimal oil."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("steamed_idli_rice_urad", "Idli", 120f),
              RecipeIngredientItem("toor_dal_cooked", "Toor Dal", 100f),
              RecipeIngredientItem("drumstick_moringa", "Drumstick", 50f),
              RecipeIngredientItem("tomato_fresh", "Tomato", 40f),
              RecipeIngredientItem("table_salt", "Salt", 0.6f)
            )
          ).first,
          primaryTags = listOf("High Fiber", "Steamed", "Fermented")
        ),
        Recipe(
          id = "rec_brk_ragi_porridge",
          title = "Finger Millet (Ragi) Porridge with Roasted Almonds",
          description = "Rich in calcium, low glycemic index, completely gluten-free Kerala morning meal.",
          mealType = "Breakfast",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("ragi_flour_cooked", "Cooked Ragi (Finger Millet) Porridge", 250f, "250g"),
            RecipeIngredientItem("curd_skimmed_yogurt", "Skimmed Buttermilk / Dahi", 100f, "100g"),
            RecipeIngredientItem("almonds_raw", "Crushed Raw Almonds", 15f, "15g"),
            RecipeIngredientItem("table_salt", "Salt", 0.4f, "0.4g")
          ),
          preparationSteps = listOf(
            "Whisk ragi flour in water and cook over low heat until glossy and thick.",
            "Allow to cool to room temperature, stir in skimmed buttermilk and a pinch of salt.",
            "Garnish with chopped raw almonds for sustained energy release."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("ragi_flour_cooked", "Ragi", 250f),
              RecipeIngredientItem("curd_skimmed_yogurt", "Curd", 100f),
              RecipeIngredientItem("almonds_raw", "Almonds", 15f),
              RecipeIngredientItem("table_salt", "Salt", 0.4f)
            )
          ).first,
          primaryTags = listOf("Low GI", "Calcium Rich", "Gluten Free")
        ),
        Recipe(
          id = "rec_brk_oats_moong_upma",
          title = "Rolled Oats & Sprouted Moong Dal Upma",
          description = "Fiber-rich oats tossed with sprouted green gram and sautéed Kerala garden vegetables.",
          mealType = "Breakfast",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("rolled_oats_cooked", "Rolled Oats (Cooked)", 150f, "150g"),
            RecipeIngredientItem("moong_dal_cooked", "Cooked Moong Dal", 80f, "80g"),
            RecipeIngredientItem("green_beans", "French Green Beans", 40f, "40g"),
            RecipeIngredientItem("olive_oil", "Extra Virgin Olive Oil", 5f, "5g"),
            RecipeIngredientItem("table_salt", "Salt", 0.5f, "0.5g")
          ),
          preparationSteps = listOf(
            "Dry roast rolled oats until fragrant.",
            "Heat olive oil in a pan, add mustard seeds, curry leaves, and chopped green beans.",
            "Add cooked moong dal and oats with 1/2 cup warm water; cook for 4 minutes."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("rolled_oats_cooked", "Oats", 150f),
              RecipeIngredientItem("moong_dal_cooked", "Moong Dal", 80f),
              RecipeIngredientItem("green_beans", "Green Beans", 40f),
              RecipeIngredientItem("olive_oil", "Olive Oil", 5f),
              RecipeIngredientItem("table_salt", "Salt", 0.5f)
            )
          ).first,
          primaryTags = listOf("Heart Healthy", "Beta-Glucan", "High Protein")
        )
      )

      "lunch" -> listOf(
        Recipe(
          id = "rec_lun_matta_fish_thoran",
          title = "Kerala Matta Red Rice with Neymeen Fish Curry & Cabbage Thoran",
          description = "Authentic Kerala meal featuring unpolished low-GI red rice, fresh seer fish cooked in Kudampuli (Garcinia) gravy, and coconut-cabbage thoran.",
          mealType = "Lunch",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("matta_rice_cooked", "Cooked Kerala Red Matta Rice", 150f, "150g"),
            RecipeIngredientItem("seer_fish_neymeen", "Fresh Seer Fish (Neymeen)", 120f, "120g"),
            RecipeIngredientItem("cabbage_raw", "Shredded Cabbage", 80f, "80g"),
            RecipeIngredientItem("coconut_grated_fresh", "Fresh Grated Coconut", 15f, "15g"),
            RecipeIngredientItem("coconut_oil", "Pure Coconut Oil (measured)", 5f, "5g"),
            RecipeIngredientItem("table_salt", "Iodized Salt", 0.8f, "0.8g")
          ),
          preparationSteps = listOf(
            "Boil washed Matta rice until grains are soft yet firm.",
            "Simmer seer fish steaks with Kudampuli (Kerala cocum), ginger, shallots, and chili powder without excessive coconut paste.",
            "Quick-sauté shredded cabbage with grated coconut, curry leaves, and green chili."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("matta_rice_cooked", "Matta Rice", 150f),
              RecipeIngredientItem("seer_fish_neymeen", "Seer Fish", 120f),
              RecipeIngredientItem("cabbage_raw", "Cabbage", 80f),
              RecipeIngredientItem("coconut_grated_fresh", "Coconut", 15f),
              RecipeIngredientItem("coconut_oil", "Oil", 5f),
              RecipeIngredientItem("table_salt", "Salt", 0.8f)
            )
          ).first,
          primaryTags = listOf("Omega-3", "Low GI", "Lean Protein")
        ),
        Recipe(
          id = "rec_lun_brown_rice_sambar_avial",
          title = "Brown Rice with Vegetable Sambar & Light Avial",
          description = "Vegetarian balanced lunch with mixed drumstick sambar, fiber-rich avial and skimmed curd.",
          mealType = "Lunch",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("brown_rice_cooked", "Cooked Brown Rice", 150f, "150g"),
            RecipeIngredientItem("toor_dal_cooked", "Cooked Toor Dal Sambar", 100f, "100g"),
            RecipeIngredientItem("green_beans", "French Beans for Avial", 60f, "60g"),
            RecipeIngredientItem("drumstick_moringa", "Moringa Pods", 40f, "40g"),
            RecipeIngredientItem("coconut_grated_fresh", "Grated Coconut for Avial", 15f, "15g"),
            RecipeIngredientItem("curd_skimmed_yogurt", "Skimmed Curd for Avial Gravy", 40f, "40g"),
            RecipeIngredientItem("table_salt", "Salt", 0.7f, "0.7g")
          ),
          preparationSteps = listOf(
            "Pressure cook brown rice until thoroughly cooked.",
            "Prepare Kerala Sambar with drumstick and toor dal.",
            "Steam beans and drumstick pieces, coat with crushed coconut, cumin, and whisked yogurt for light avial."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("brown_rice_cooked", "Brown Rice", 150f),
              RecipeIngredientItem("toor_dal_cooked", "Toor Dal", 100f),
              RecipeIngredientItem("green_beans", "Beans", 60f),
              RecipeIngredientItem("drumstick_moringa", "Moringa", 40f),
              RecipeIngredientItem("coconut_grated_fresh", "Coconut", 15f),
              RecipeIngredientItem("curd_skimmed_yogurt", "Curd", 40f),
              RecipeIngredientItem("table_salt", "Salt", 0.7f)
            )
          ).first,
          primaryTags = listOf("Vegetarian", "High Fiber", "Gut Friendly")
        )
      )

      "dinner" -> listOf(
        Recipe(
          id = "rec_din_roti_chicken_cheera",
          title = "Whole Wheat Atta Roti with Grilled Chicken & Cheera (Amaranth) Thoran",
          description = "Light balanced dinner high in iron and lean protein to support overnight satiety.",
          mealType = "Dinner",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("atta_whole_wheat_roti", "Whole Wheat Roti (2 pcs)", 70f, "70g"),
            RecipeIngredientItem("chicken_breast_skinless", "Skinless Chicken Breast", 120f, "120g"),
            RecipeIngredientItem("spinach_cheera", "Fresh Red Cheera (Amaranth)", 100f, "100g"),
            RecipeIngredientItem("olive_oil", "Olive Oil", 5f, "5g"),
            RecipeIngredientItem("table_salt", "Salt", 0.7f, "0.7g")
          ),
          preparationSteps = listOf(
            "Knead whole wheat flour with warm water and puff on dry tava without oil.",
            "Marinate chicken breast with lemon, turmeric, pepper and pan grill.",
            "Sauté chopped amaranth leaves (cheera) with garlic and cumin until wilted."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("atta_whole_wheat_roti", "Roti", 70f),
              RecipeIngredientItem("chicken_breast_skinless", "Chicken", 120f),
              RecipeIngredientItem("spinach_cheera", "Cheera", 100f),
              RecipeIngredientItem("olive_oil", "Olive Oil", 5f),
              RecipeIngredientItem("table_salt", "Salt", 0.7f)
            )
          ).first,
          primaryTags = listOf("High Protein", "Iron Rich", "Low Calorie")
        ),
        Recipe(
          id = "rec_din_matta_kadala_curry",
          title = "Kerala Matta Rice with Kadala Curry & Steamed Amaranth",
          description = "Plant-based protein bowl featuring slow-simmered black chickpeas and garden greens.",
          mealType = "Dinner",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("matta_rice_cooked", "Kerala Matta Rice", 120f, "120g"),
            RecipeIngredientItem("kadala_black_chickpeas", "Black Chickpeas (Kadala)", 110f, "110g"),
            RecipeIngredientItem("spinach_cheera", "Amaranth Greens (Cheera)", 80f, "80g"),
            RecipeIngredientItem("olive_oil", "Extra Virgin Olive Oil", 5f, "5g"),
            RecipeIngredientItem("table_salt", "Salt", 0.6f, "0.6g")
          ),
          preparationSteps = listOf(
            "Boil soaked black chickpeas with shallots and coriander until tender.",
            "Simmer in mild Kerala roasted spice gravy.",
            "Serve with steamed red rice and sautéed greens."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("matta_rice_cooked", "Matta Rice", 120f),
              RecipeIngredientItem("kadala_black_chickpeas", "Kadala", 110f),
              RecipeIngredientItem("spinach_cheera", "Cheera", 80f),
              RecipeIngredientItem("olive_oil", "Olive Oil", 5f),
              RecipeIngredientItem("table_salt", "Salt", 0.6f)
            )
          ).first,
          primaryTags = listOf("Plant Protein", "Low GI", "High Fiber")
        )
      )

      else -> listOf( // Snacks
        Recipe(
          id = "rec_snk_moong_sundal",
          title = "Sprouted Moong Dal Sundal with Curry Leaves",
          description = "Zero cholesterol, quick-digestible evening snack seasoned with mustard seeds.",
          mealType = "Snack",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("moong_dal_cooked", "Sprouted Green Moong (Cooked)", 100f, "100g"),
            RecipeIngredientItem("coconut_grated_fresh", "Fresh Grated Coconut", 10f, "10g"),
            RecipeIngredientItem("olive_oil", "Olive Oil", 3f, "3g"),
            RecipeIngredientItem("table_salt", "Salt", 0.3f, "0.3g")
          ),
          preparationSteps = listOf(
            "Steam sprouted green gram for 5 minutes.",
            "Toss in pan with mustard seeds, curry leaves, and fresh grated coconut."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("moong_dal_cooked", "Moong Dal", 100f),
              RecipeIngredientItem("coconut_grated_fresh", "Coconut", 10f),
              RecipeIngredientItem("olive_oil", "Olive Oil", 3f),
              RecipeIngredientItem("table_salt", "Salt", 0.3f)
            )
          ).first,
          primaryTags = listOf("Gluten Free", "Heart Healthy", "High Fiber")
        ),
        Recipe(
          id = "rec_snk_almond_chia",
          title = "Roasted California Almonds & Chia Water",
          description = "Crunchy raw almonds providing healthy monounsaturated fats and magnesium.",
          mealType = "Snack",
          cuisineStyle = "Indian Balanced",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("almonds_raw", "Raw California Almonds", 25f, "25g"),
            RecipeIngredientItem("chia_seeds", "Chia Seeds (Soaked in lime water)", 10f, "10g")
          ),
          preparationSteps = listOf(
            "Soak chia seeds in 250ml water with fresh lime juice for 15 minutes.",
            "Serve with unpeeled raw almonds."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("almonds_raw", "Almonds", 25f),
              RecipeIngredientItem("chia_seeds", "Chia Seeds", 10f)
            )
          ).first,
          primaryTags = listOf("Low Carb", "Brain Health", "Keto Friendly")
        )
      )
    }
  }
}
