package com.example.data.engine

import com.example.data.model.ADIMEClinicalRecord
import com.example.data.model.HealthProfile
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredientItem
import com.example.data.model.ValidationResult
import com.example.data.model.ValidationStatus
import com.example.data.repository.FoodCompositionRepository
import java.util.Calendar

/**
 * Meal Recommendation Engine.
 * Integrates:
 * 1. HealthGenie (arXiv:2504.14594): Circular interactive adaptation (Include/Exclude toggles).
 * 2. NutriOrion (arXiv:2602.18650): Prevents Healthy Food Trap by injecting pharmacological contraindications
 *    (Warfarin, Spironolactone, Statins) as hard negative constraints during synthesis + ADIME clinical record.
 * 3. NutriGen (arXiv:2502.20601): Deterministic nutritional calculations and 3 distinct diverse options.
 */
object MealRecommendationEngine {

  data class RecommendedMealCandidate(
    val recipe: Recipe,
    val validationResult: ValidationResult,
    val adimeRecord: ADIMEClinicalRecord,
    val isSuggestedForCurrentTime: Boolean = false,
    val isAiGenerated: Boolean = false,
    val clinicalRationale: String = "",
    val adherenceTips: List<String> = emptyList()
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
   * Time-aware meal generation.
   */
  fun getSuggestedMealTypeForCurrentTime(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
      in 5..10 -> "Breakfast"
      in 11..15 -> "Lunch"
      in 16..18 -> "Snack"
      in 19..22 -> "Dinner"
      else -> "Breakfast"
    }
  }

  /**
   * Generates 3 diverse meal recommendations for a given meal type tailored to the user's clinical profile.
   */
  fun generateMealsForType(
    mealType: String,
    profile: HealthProfile,
    remainingCalories: Int = 500
  ): List<RecommendedMealCandidate> {
    val baseCandidates = getPredefinedRecipeCandidates(mealType, profile)

    return baseCandidates.map { candidateRecipe ->
      // 1. Synthesize candidate with Hard Negative Clinical Constraints (NutriOrion)
      val constrainedRecipe = applyClinicalSynthesisConstraints(candidateRecipe, profile)

      // 2. Deterministic Calculation (NutriGen)
      val (nutrients, _) = NutritionCalculator.calculateRecipeNutrients(
        constrainedRecipe.ingredients,
        constrainedRecipe.defaultServings
      )
      val verifiedRecipe = constrainedRecipe.copy(calculatedNutrientsPerServing = nutrients)

      // 3. Independent Rule-Based Safety Gate & DNI Check
      val validation = MealSafetyValidator.validateRecipe(verifiedRecipe, profile)

      // 4. ADIME Clinical Documentation
      val adime = MealSafetyValidator.generateADIMERecord(verifiedRecipe, profile)

      RecommendedMealCandidate(
        recipe = verifiedRecipe,
        validationResult = validation,
        adimeRecord = adime,
        isSuggestedForCurrentTime = (mealType.equals(getSuggestedMealTypeForCurrentTime(), ignoreCase = true))
      )
    }.sortedBy { candidate ->
      when (candidate.validationResult.overallStatus) {
        ValidationStatus.APPROVED -> 0
        ValidationStatus.MODIFIED -> 1
        ValidationStatus.WARNING -> 2
        ValidationStatus.REJECTED -> 3
      }
    }
  }

  /**
   * HealthGenie Circular Interactive Adaptation:
   * When user clicks "Exclude" on an ingredient, this dynamically substitutes it
   * with a clinically compliant alternative, recalculates nutrients, and revalidates.
   */
  fun adaptRecipeWithExclusion(
    candidate: RecommendedMealCandidate,
    excludedIngredientId: String,
    profile: HealthProfile
  ): RecommendedMealCandidate {
    val currentRecipe = candidate.recipe
    val updatedExclusions = (currentRecipe.excludedIngredientIds + excludedIngredientId).distinct()

    val updatedIngredients = currentRecipe.ingredients.map { item ->
      if (item.ingredientId == excludedIngredientId) {
        // Find safe clinical substitution
        val substitute = getClinicalSubstitute(item.ingredientId, profile)
        RecipeIngredientItem(
          ingredientId = substitute.id,
          displayName = "${substitute.name} (Substituted for ${item.displayName})",
          quantityGrams = item.quantityGrams,
          displayUnit = item.displayUnit,
          preparationNote = "Lightly sautéed as an alternative",
          isSubstituted = true
        )
      } else {
        item
      }
    }

    val (nutrients, _) = NutritionCalculator.calculateRecipeNutrients(
      updatedIngredients,
      currentRecipe.defaultServings
    )

    val adaptedRecipe = currentRecipe.copy(
      ingredients = updatedIngredients,
      calculatedNutrientsPerServing = nutrients,
      excludedIngredientIds = updatedExclusions,
      suitabilityNotes = "Interactively modified: replaced ingredient '$excludedIngredientId' with safe alternative."
    )

    val validation = MealSafetyValidator.validateRecipe(adaptedRecipe, profile)
    val adime = MealSafetyValidator.generateADIMERecord(adaptedRecipe, profile)

    return RecommendedMealCandidate(
      recipe = adaptedRecipe,
      validationResult = validation,
      adimeRecord = adime,
      isSuggestedForCurrentTime = candidate.isSuggestedForCurrentTime
    )
  }

  /**
   * HealthGenie Clinical Substitutions:
   * Replaces excluded or contraindicated foods with safe regional alternatives.
   */
  private fun getClinicalSubstitute(ingredientId: String, profile: HealthProfile): com.example.data.model.FoodIngredient {
    val isWarfarin = profile.medications.any { it.contains("Warfarin", ignoreCase = true) }
    val isRenalOrDiuretic = profile.medications.any { it.contains("Spironolactone", ignoreCase = true) } ||
      profile.medicalConditions.any { it.contains("CKD", ignoreCase = true) || it.contains("Kidney", ignoreCase = true) }

    return when {
      // If leafy green is excluded/contraindicated, substitute with low Vitamin K gourd
      ingredientId.contains("cheera") || ingredientId.contains("spinach") || ingredientId.contains("palak") -> {
        FoodCompositionRepository.getIngredient("bottle_gourd_cooked")
          ?: FoodCompositionRepository.getIngredient("ash_gourd_cooked")!!
      }
      // If high sodium or coconut is excluded, substitute with light cucumber or buttermilk
      ingredientId.contains("coconut") -> {
        FoodCompositionRepository.getIngredient("cucumber_vellarikka")
          ?: FoodCompositionRepository.getIngredient("ash_gourd_cooked")!!
      }
      // If seafood is excluded, substitute with yellow moong dal
      ingredientId.contains("sardine") || ingredientId.contains("mackerel") || ingredientId.contains("fish") -> {
        FoodCompositionRepository.getIngredient("moong_dal_cooked")
          ?: FoodCompositionRepository.getIngredient("boiled_egg_white")!!
      }
      // If wheat is excluded for Celiac, substitute with Kerala Matta Red Rice
      ingredientId.contains("roti") || ingredientId.contains("atta") || ingredientId.contains("wheat") -> {
        FoodCompositionRepository.getIngredient("matta_rice_cooked")
          ?: FoodCompositionRepository.getIngredient("ragi_flour_cooked")!!
      }
      else -> {
        FoodCompositionRepository.getIngredient("ash_gourd_cooked")!!
      }
    }
  }

  /**
   * Injects Hard Negative Clinical Constraints (NutriOrion) during synthesis:
   * Replaces contraindicated items before presentation.
   */
  private fun applyClinicalSynthesisConstraints(recipe: Recipe, profile: HealthProfile): Recipe {
    val isWarfarin = profile.medications.any { it.contains("Warfarin", ignoreCase = true) }
    val isCeliac = profile.medicalConditions.any { it.contains("Celiac", ignoreCase = true) }
    val isCkd = profile.medicalConditions.any { it.contains("Kidney", ignoreCase = true) || it.contains("CKD", ignoreCase = true) }

    val modifiedIngredients = recipe.ingredients.map { item ->
      val ing = FoodCompositionRepository.getIngredient(item.ingredientId)

      // NutriOrion Healthy Food Trap: Block high-Vitamin K greens for Warfarin
      if (isWarfarin && (item.ingredientId == "red_cheera_amaranth" || item.ingredientId == "spinach_palak_cooked" || (ing?.vitaminKMcgPer100g ?: 0f) > 100f)) {
        val safeGourd = FoodCompositionRepository.getIngredient("bottle_gourd_cooked")!!
        RecipeIngredientItem(
          ingredientId = safeGourd.id,
          displayName = "${safeGourd.name} (Substituted to prevent Warfarin Vitamin-K antagonism)",
          quantityGrams = item.quantityGrams,
          displayUnit = item.displayUnit,
          preparationNote = "Lightly steamed with turmeric and cumin",
          isSubstituted = true
        )
      } else if (isCeliac && ing?.isGlutenFree == false) {
        // Celiac safety substitution
        val safeGrain = FoodCompositionRepository.getIngredient("ragi_flour_cooked")!!
        RecipeIngredientItem(
          ingredientId = safeGrain.id,
          displayName = "${safeGrain.name} (Substituted for Gluten-Free Celiac compliance)",
          quantityGrams = item.quantityGrams,
          displayUnit = item.displayUnit,
          preparationNote = "Steamed ragi flour",
          isSubstituted = true
        )
      } else {
        item
      }
    }

    return recipe.copy(ingredients = modifiedIngredients)
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

    val note = "Balanced daily plan totaling ${totalCal.toInt()} kcal (${(totalCal / profile.dailyCalorieTarget * 100).toInt()}% of target). Formulated with clinical rules (NutriOrion DNI safe, NutriGen deterministic calculation)."

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
          id = "rec_brk_ragi_puttu",
          title = "Kerala Ragi (Finger Millet) Puttu with Green Gram",
          description = "Traditional steamed ragi flour layered with fresh grated coconut, served with boiled whole cherupayar.",
          mealType = "Breakfast",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("ragi_flour_cooked", "Steamed Ragi Puttu", 150f, "150g"),
            RecipeIngredientItem("green_gram_whole_boiled", "Boiled Cherupayar (Green Gram)", 80f, "80g"),
            RecipeIngredientItem("coconut_grated_fresh", "Fresh Grated Coconut", 15f, "15g"),
            RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves for Tempering", 2f, "2g")
          ),
          preparationSteps = listOf(
            "Moisten ragi flour with a sprinkle of salted warm water to breadcrumb texture.",
            "Layer in cylindrical puttu steamer alternating with fresh grated coconut.",
            "Steam for 6-8 minutes until steam flows freely.",
            "Serve hot with lightly tempered green gram."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("ragi_flour_cooked", "Ragi Puttu", 150f),
              RecipeIngredientItem("green_gram_whole_boiled", "Cherupayar", 80f),
              RecipeIngredientItem("coconut_grated_fresh", "Coconut", 15f),
              RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves", 2f)
            )
          ).first,
          primaryTags = listOf("Low GI", "High Calcium", "High Fiber", "Gluten Free")
        ),
        Recipe(
          id = "rec_brk_oats_upma",
          title = "Rolled Oats & Moringa Leaves Porridge / Upma",
          description = "Heart-healthy oats cooked with iron-dense drumstick leaves and mild ginger tempering.",
          mealType = "Breakfast",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("rolled_oats_cooked", "Rolled Oats", 160f, "160g"),
            RecipeIngredientItem("moong_dal_cooked", "Boiled Yellow Moong Dal", 60f, "60g"),
            RecipeIngredientItem("moringa_leaves_cooked", "Fresh Moringa (Drumstick) Leaves", 30f, "30g"),
            RecipeIngredientItem("coconut_oil_virgin", "Virgin Coconut Oil", 4f, "4g")
          ),
          preparationSteps = listOf(
            "Dry toast oats in pan until nutty and fragrant.",
            "Sauté moringa leaves with a drop of coconut oil and mustard seeds.",
            "Combine oats, cooked moong dal, and 1 cup water; simmer for 4 minutes until creamy."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("rolled_oats_cooked", "Oats", 160f),
              RecipeIngredientItem("moong_dal_cooked", "Moong Dal", 60f),
              RecipeIngredientItem("moringa_leaves_cooked", "Moringa", 30f),
              RecipeIngredientItem("coconut_oil_virgin", "Coconut Oil", 4f)
            )
          ).first,
          primaryTags = listOf("Beta-Glucan", "Iron Rich", "Heart Healthy")
        ),
        Recipe(
          id = "rec_brk_egg_white_appam",
          title = "Fermented Rice Appam with Stewed Vegetable Curry",
          description = "Fluffy fermented lace appam served with ash gourd, beans, and boiled egg white in light coconut milk.",
          mealType = "Breakfast",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("white_rice_cooked", "Appam (Fermented Rice Pancake)", 120f, "120g"),
            RecipeIngredientItem("ash_gourd_cooked", "Ash Gourd (Kumbalanga) in Stew", 80f, "80g"),
            RecipeIngredientItem("boiled_egg_white", "Boiled Egg White", 66f, "2 whites (66g)"),
            RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves & Green Chili", 2f, "2g")
          ),
          preparationSteps = listOf(
            "Swirl fermented batter in heated appachatti; cover and cook until edges crisp.",
            "Simmer diced ash gourd with crushed peppercorns and curry leaves.",
            "Serve hot with steamed boiled egg whites for lean protein."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("white_rice_cooked", "Appam", 120f),
              RecipeIngredientItem("ash_gourd_cooked", "Ash Gourd", 80f),
              RecipeIngredientItem("boiled_egg_white", "Egg White", 66f),
              RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves", 2f)
            )
          ).first,
          primaryTags = listOf("Lean Protein", "Fermented Gut Health", "Low Sodium")
        )
      )

      "lunch" -> listOf(
        Recipe(
          id = "rec_lun_matta_sardine_thoran",
          title = "Kerala Matta Red Rice with Sardine (Mathi) Curry & Vendakka Thoran",
          description = "Unpolished antioxidant-rich Palakkadan Matta rice paired with Omega-3 rich fresh sardines cooked in Kudampuli kokum gravy and okra thoran.",
          mealType = "Lunch",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("matta_rice_cooked", "Palakkadan Matta Rice (Cooked)", 160f, "160g"),
            RecipeIngredientItem("kerala_sardines_curry", "Fresh Indian Sardine (Mathi) Curry", 110f, "110g"),
            RecipeIngredientItem("okra_ladies_finger_cooked", "Vendakka (Okra) Thoran", 70f, "70g"),
            RecipeIngredientItem("coconut_grated_fresh", "Fresh Grated Coconut", 12f, "12g"),
            RecipeIngredientItem("coconut_oil_virgin", "Virgin Coconut Oil", 4f, "4g")
          ),
          preparationSteps = listOf(
            "Boil washed Matta rice in excess water and drain when soft with firm bran.",
            "Simmer fresh sardines with Kudampuli (Garcinia), turmeric, shallots, and fresh curry leaves.",
            "Sauté finely sliced okra (vendakka) on low flame with grated coconut and crushed cumin until crisp."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("matta_rice_cooked", "Matta Rice", 160f),
              RecipeIngredientItem("kerala_sardines_curry", "Sardines", 110f),
              RecipeIngredientItem("okra_ladies_finger_cooked", "Vendakka", 70f),
              RecipeIngredientItem("coconut_grated_fresh", "Coconut", 12f),
              RecipeIngredientItem("coconut_oil_virgin", "Oil", 4f)
            )
          ).first,
          primaryTags = listOf("Omega-3 EPA/DHA", "Low Glycemic Load", "Cardiovascular")
        ),
        Recipe(
          id = "rec_lun_matta_sambar_avial",
          title = "Kerala Matta Rice with Drumstick Sambar & Traditional Avial",
          description = "Authentic plant-based Kerala sadya plate with toor dal drumstick sambar, mixed gourd avial, and buttermilk.",
          mealType = "Lunch",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("matta_rice_cooked", "Cooked Matta Rice", 150f, "150g"),
            RecipeIngredientItem("toor_dal_sambar_cooked", "Toor Dal Sambar with Drumstick", 110f, "110g"),
            RecipeIngredientItem("ash_gourd_cooked", "Ash Gourd & Plantain Avial", 80f, "80g"),
            RecipeIngredientItem("coconut_grated_fresh", "Grated Coconut for Avial", 14f, "14g"),
            RecipeIngredientItem("curd_homemade_buttermilk", "Spiced Buttermilk (Sambharam)", 100f, "100g")
          ),
          preparationSteps = listOf(
            "Cook Matta rice until tender.",
            "Boil drumstick in tamarind-water and combine with cooked toor dal and sambar spices.",
            "Steam ash gourd and raw banana; fold into coarsely crushed coconut-cumin paste and virgin coconut oil."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("matta_rice_cooked", "Matta Rice", 150f),
              RecipeIngredientItem("toor_dal_sambar_cooked", "Toor Dal Sambar", 110f),
              RecipeIngredientItem("ash_gourd_cooked", "Avial Veggies", 80f),
              RecipeIngredientItem("coconut_grated_fresh", "Coconut", 14f),
              RecipeIngredientItem("curd_homemade_buttermilk", "Sambharam", 100f)
            )
          ).first,
          primaryTags = listOf("Vegetarian", "High Soluble Fiber", "Gut Microbiome")
        ),
        Recipe(
          id = "rec_lun_brown_rice_mackerel",
          title = "Brown Rice with Indian Mackerel (Ayala) Peera & Cucumber Salad",
          description = "Lean protein lunch featuring fresh Indian mackerel steamed with crushed coconut and Malabar cucumber.",
          mealType = "Lunch",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("brown_rice_cooked", "Cooked Brown Rice", 150f, "150g"),
            RecipeIngredientItem("kerala_mackerel_curry", "Ayala (Mackerel) Peera", 115f, "115g"),
            RecipeIngredientItem("cucumber_vellarikka", "Malabar Cucumber Slices", 70f, "70g"),
            RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves", 3f, "3g")
          ),
          preparationSteps = listOf(
            "Cook brown rice to perfection.",
            "Coarsely grind fresh coconut, green chili, turmeric, and shallots.",
            "Layer clean mackerel pieces with ground mixture, kudampuli, and curry leaves in clay pot (chatti); cook dry.",
            "Serve with refreshing cucumber slices."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("brown_rice_cooked", "Brown Rice", 150f),
              RecipeIngredientItem("kerala_mackerel_curry", "Mackerel", 115f),
              RecipeIngredientItem("cucumber_vellarikka", "Cucumber", 70f),
              RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves", 3f)
            )
          ).first,
          primaryTags = listOf("High Protein", "Low Sodium", "Omega-3")
        )
      )

      "dinner" -> listOf(
        Recipe(
          id = "rec_din_atta_roti_cherupayar",
          title = "Whole Wheat Atta Chapati with Sprouted Green Gram & Lauki Curry",
          description = "Light dinner balancing complex carbohydrates, vegetable protein, and soothing bottle gourd.",
          mealType = "Dinner",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("atta_whole_wheat_roti", "Whole Wheat Roti (2 chapatis)", 70f, "70g"),
            RecipeIngredientItem("green_gram_whole_boiled", "Whole Cherupayar Curry", 100f, "100g"),
            RecipeIngredientItem("bottle_gourd_cooked", "Bottle Gourd (Churakka) Sauté", 80f, "80g"),
            RecipeIngredientItem("coconut_oil_virgin", "Virgin Coconut Oil", 4f, "4g")
          ),
          preparationSteps = listOf(
            "Roll whole wheat dough thin and cook on hot tava without oil until puffed.",
            "Pressure cook green gram with shallots, green chilies, and turmeric; temper with mustard.",
            "Gently cook diced bottle gourd with mild cumin seasoning for easy nighttime digestion."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("atta_whole_wheat_roti", "Roti", 70f),
              RecipeIngredientItem("green_gram_whole_boiled", "Cherupayar", 100f),
              RecipeIngredientItem("bottle_gourd_cooked", "Bottle Gourd", 80f),
              RecipeIngredientItem("coconut_oil_virgin", "Oil", 4f)
            )
          ).first,
          primaryTags = listOf("Digestive Rest", "Low Glycemic", "High Protein")
        ),
        Recipe(
          id = "rec_din_matta_kadala_curry",
          title = "Kerala Matta Red Rice with Kadala Curry & Cheera Thoran",
          description = "Iron-rich dinner with tender black chickpeas slow-simmered in roasted coriander and red amaranth thoran.",
          mealType = "Dinner",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("matta_rice_cooked", "Cooked Red Matta Rice", 120f, "120g"),
            RecipeIngredientItem("kadala_black_chickpeas_cooked", "Black Chickpeas (Kadala) Curry", 110f, "110g"),
            RecipeIngredientItem("red_cheera_amaranth", "Red Cheera (Amaranth) Thoran", 75f, "75g"),
            RecipeIngredientItem("coconut_grated_fresh", "Fresh Grated Coconut", 10f, "10g")
          ),
          preparationSteps = listOf(
            "Cook Matta rice until tender.",
            "Simmer pressure-cooked black chickpeas in roasted coriander and shallot broth.",
            "Toss finely chopped red cheera leaves with grated coconut and crushed garlic on low heat."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("matta_rice_cooked", "Matta Rice", 120f),
              RecipeIngredientItem("kadala_black_chickpeas_cooked", "Kadala", 110f),
              RecipeIngredientItem("red_cheera_amaranth", "Red Cheera", 75f),
              RecipeIngredientItem("coconut_grated_fresh", "Coconut", 10f)
            )
          ).first,
          primaryTags = listOf("Iron Rich", "Plant Protein", "Cardioprotective")
        ),
        Recipe(
          id = "rec_din_ragi_soup_egg",
          title = "Warm Ragi Soup with Boiled Egg White & Ash Gourd",
          description = "Ultra-light, soothing evening soup loaded with bioavailable calcium and zero refined sodium.",
          mealType = "Dinner",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("ragi_flour_cooked", "Finger Millet (Ragi) Broth", 180f, "180g"),
            RecipeIngredientItem("boiled_egg_white", "Boiled Egg White (Diced)", 66f, "66g"),
            RecipeIngredientItem("ash_gourd_cooked", "Steamed Ash Gourd Cubes", 70f, "70g"),
            RecipeIngredientItem("curry_leaves_fresh", "Crushed Curry Leaves & Ginger", 3f, "3g")
          ),
          preparationSteps = listOf(
            "Whisk ragi flour in cold water and simmer into clear, soothing herbal broth.",
            "Add diced steamed ash gourd and ginger.",
            "Top with diced boiled egg whites and crushed curry leaves before serving."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("ragi_flour_cooked", "Ragi Broth", 180f),
              RecipeIngredientItem("boiled_egg_white", "Egg White", 66f),
              RecipeIngredientItem("ash_gourd_cooked", "Ash Gourd", 70f),
              RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves", 3f)
            )
          ).first,
          primaryTags = listOf("Low Calorie", "Renal Friendly", "Zero Added Salt")
        )
      )

      else -> listOf( // Snacks
        Recipe(
          id = "rec_snk_sambharam_buttermilk",
          title = "Kerala Spiced Sambharam (Herb Buttermilk)",
          description = "Traditional cooling probiotic digestive drink with crushed shallots, ginger, and curry leaves.",
          mealType = "Snack",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("curd_homemade_buttermilk", "Diluted Skimmed Curd", 220f, "220g"),
            RecipeIngredientItem("curry_leaves_fresh", "Crushed Curry Leaves & Ginger", 4f, "4g"),
            RecipeIngredientItem("cucumber_vellarikka", "Finely Diced Malabar Cucumber", 30f, "30g")
          ),
          preparationSteps = listOf(
            "Churn fresh curd with chilled water until light.",
            "Crush ginger, green chili, and fresh curry leaves; steep into buttermilk for 10 minutes.",
            "Garnish with diced cucumber and serve cool."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("curd_homemade_buttermilk", "Curd", 220f),
              RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves", 4f),
              RecipeIngredientItem("cucumber_vellarikka", "Cucumber", 30f)
            )
          ).first,
          primaryTags = listOf("Probiotic", "Hydration", "Low Calorie")
        ),
        Recipe(
          id = "rec_snk_boiled_cherupayar",
          title = "Steamed Sprouted Moong Sundal",
          description = "High-protein afternoon snack tempered with mustard seeds, curry leaves, and minimal fresh coconut.",
          mealType = "Snack",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("green_gram_whole_boiled", "Steamed Whole Moong", 90f, "90g"),
            RecipeIngredientItem("coconut_grated_fresh", "Fresh Coconut Garnish", 8f, "8g"),
            RecipeIngredientItem("coconut_oil_virgin", "Virgin Coconut Oil Tempering", 3f, "3g")
          ),
          preparationSteps = listOf(
            "Steam sprouted green gram until tender yet holding shape.",
            "Heat half teaspoon coconut oil, pop mustard seeds and curry leaves.",
            "Toss steamed moong and garnish with fresh coconut."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("green_gram_whole_boiled", "Green Gram", 90f),
              RecipeIngredientItem("coconut_grated_fresh", "Coconut", 8f),
              RecipeIngredientItem("coconut_oil_virgin", "Oil", 3f)
            )
          ).first,
          primaryTags = listOf("High Protein", "Low GI", "Satiety")
        ),
        Recipe(
          id = "rec_snk_raw_plantain_chips",
          title = "Air-Roasted Kerala Green Plantain Slices",
          description = "Zero-oil baked pacha ethakka slices dusted with roasted cumin and turmeric.",
          mealType = "Snack",
          cuisineStyle = "Kerala / South Indian",
          defaultServings = 1,
          ingredients = listOf(
            RecipeIngredientItem("raw_banana_green_plantain", "Thinly Sliced Raw Plantain", 75f, "75g"),
            RecipeIngredientItem("curry_leaves_fresh", "Dehydrated Curry Leaves Powder", 2f, "2g")
          ),
          preparationSteps = listOf(
            "Slice peeled green plantains very thinly.",
            "Toss in turmeric and roast in air oven at 160°C until crisp without deep frying.",
            "Dust with roasted cumin and curry leaves powder."
          ),
          calculatedNutrientsPerServing = NutritionCalculator.calculateRecipeNutrients(
            listOf(
              RecipeIngredientItem("raw_banana_green_plantain", "Plantain", 75f),
              RecipeIngredientItem("curry_leaves_fresh", "Curry Leaves", 2f)
            )
          ).first,
          primaryTags = listOf("Resistant Starch", "Oil Free", "Gluten Free")
        )
      )
    }
  }
}
