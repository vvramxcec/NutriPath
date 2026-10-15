package com.example.data.model

/**
 * Verified food composition reference data per 100g.
 * Sourced from Indian Food Composition Tables (IFCT / ICMR-NIN) and USDA FoodData Central.
 */
data class FoodIngredient(
  val id: String,
  val name: String,
  val category: String, // Grain, Pulse, Vegetable, Dairy, Meat/Fish, Oil, Spice
  val caloriesPer100g: Float,
  val proteinPer100g: Float,
  val carbsPer100g: Float,
  val fatPer100g: Float,
  val fiberPer100g: Float,
  val sugarPer100g: Float,
  val sodiumMgPer100g: Float,
  val potassiumMgPer100g: Float,
  val saturatedFatPer100g: Float = 0f,
  val allergens: List<String> = emptyList(),
  val glycemicIndex: String = "Low", // Low, Medium, High
  val isGlutenFree: Boolean = true,
  val dataSource: String = "IFCT ICMR-NIN 2017 & USDA FoodData"
)

/**
 * An ingredient specification inside a recipe with weight in grams.
 */
data class RecipeIngredientItem(
  val ingredientId: String,
  val displayName: String,
  val quantityGrams: Float,
  val displayUnit: String = "g", // e.g., "150g", "1 cup (150g)"
  val preparationNote: String = ""
)

/**
 * Calculated aggregate nutrient values.
 */
data class CalculatedNutrients(
  val caloriesKcal: Float,
  val proteinGrams: Float,
  val carbsGrams: Float,
  val fatGrams: Float,
  val fiberGrams: Float,
  val sugarGrams: Float,
  val sodiumMg: Float,
  val potassiumMg: Float,
  val saturatedFatGrams: Float = 0f
) {
  fun scaledByServings(servings: Float): CalculatedNutrients {
    if (servings <= 0f) return this
    return CalculatedNutrients(
      caloriesKcal = caloriesKcal * servings,
      proteinGrams = proteinGrams * servings,
      carbsGrams = carbsGrams * servings,
      fatGrams = fatGrams * servings,
      fiberGrams = fiberGrams * servings,
      sugarGrams = sugarGrams * servings,
      sodiumMg = sodiumMg * servings,
      potassiumMg = potassiumMg * servings,
      saturatedFatGrams = saturatedFatGrams * servings
    )
  }
}

/**
 * Recipe structure containing ingredient composition and preparation.
 */
data class Recipe(
  val id: String,
  val title: String,
  val description: String,
  val mealType: String, // Breakfast, Lunch, Dinner, Snack
  val cuisineStyle: String = "Kerala / South Indian",
  val defaultServings: Int = 1,
  val ingredients: List<RecipeIngredientItem>,
  val preparationSteps: List<String>,
  val calculatedNutrientsPerServing: CalculatedNutrients,
  val suitabilityNotes: String = "",
  val primaryTags: List<String> = emptyList()
)
