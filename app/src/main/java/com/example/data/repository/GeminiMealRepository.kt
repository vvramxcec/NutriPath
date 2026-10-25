package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import com.example.data.api.GeminiApiService
import com.example.data.api.GeminiContent
import com.example.data.api.GeminiGenerateRequest
import com.example.data.api.GeminiGenerationConfig
import com.example.data.api.GeminiGeneratedMealPlanResponse
import com.example.data.api.GeminiPart
import com.example.data.engine.MealRecommendationEngine
import com.example.data.engine.MealSafetyValidator
import com.example.data.engine.NutritionCalculator
import com.example.data.model.CalculatedNutrients
import com.example.data.model.HealthProfile
import com.example.data.model.Recipe
import com.example.data.model.RecipeIngredientItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class GeminiMealRepository(
  private val apiService: GeminiApiService = GeminiApiService.create()
) {

  sealed class GenerationResult {
    data class Success(val candidates: List<MealRecommendationEngine.RecommendedMealCandidate>, val isAiGenerated: Boolean) : GenerationResult()
    data class Fallback(val candidates: List<MealRecommendationEngine.RecommendedMealCandidate>, val fallbackReason: String) : GenerationResult()
  }

  suspend fun generatePersonalizedMeals(
    mealType: String,
    profile: HealthProfile,
    customPrompt: String? = null
  ): GenerationResult = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY

    // Check if API key is unconfigured or placeholder
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      Log.w("GeminiMealRepository", "GEMINI_API_KEY is not configured or placeholder. Falling back to deterministic engine.")
      val fallbackCandidates = MealRecommendationEngine.generateMealsForType(mealType, profile)
      return@withContext GenerationResult.Fallback(
        candidates = fallbackCandidates,
        fallbackReason = "API key not configured in AI Studio Secrets. Using clinical catalog fallback."
      )
    }

    try {
      val prompt = buildClinicalPrompt(mealType, profile, customPrompt)
      val request = GeminiGenerateRequest(
        contents = listOf(
          GeminiContent(parts = listOf(GeminiPart(text = prompt)))
        ),
        generationConfig = GeminiGenerationConfig(
          temperature = 0.4f,
          responseMimeType = "application/json"
        )
      )

      val response = apiService.generateContent(apiKey, request)
      val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

      if (jsonText.isNullOrBlank()) {
        throw IllegalStateException("Empty response from Gemini API")
      }

      // Parse JSON with Moshi
      val adapter = GeminiApiService.moshi.adapter(GeminiGeneratedMealPlanResponse::class.java)
      val parsedResponse = adapter.fromJson(jsonText)

      val items = parsedResponse?.candidates.orEmpty()
      if (items.isEmpty()) {
        throw IllegalStateException("No meal candidates returned in JSON")
      }

      val candidates = items.mapIndexed { index, item ->
        val ingredientsList = item.ingredients.mapIndexed { ingIndex, ing ->
          RecipeIngredientItem(
            ingredientId = "gemini_ing_${index}_$ingIndex",
            displayName = ing.displayName.ifBlank { "Nutritious Ingredient" },
            quantityGrams = if (ing.quantityGrams > 0) ing.quantityGrams else 50f,
            displayUnit = ing.displayUnit.ifBlank { "g" },
            preparationNote = ing.preparationNote
          )
        }

        val calculatedNutrients = CalculatedNutrients(
          caloriesKcal = if (item.calories > 0) item.calories else 400f,
          proteinGrams = item.proteinGrams,
          carbsGrams = item.carbsGrams,
          fatGrams = item.fatGrams,
          fiberGrams = item.fiberGrams,
          sugarGrams = 2f,
          sodiumMg = if (item.sodiumMg > 0) item.sodiumMg else 300f,
          potassiumMg = if (item.potassiumMg > 0) item.potassiumMg else 400f
        )

        val recipe = Recipe(
          id = "gemini_rec_${UUID.randomUUID().toString().take(8)}",
          title = item.title.ifBlank { "$mealType Option ${index + 1}" },
          description = item.description.ifBlank { "Personalized clinical meal crafted for your health profile." },
          mealType = mealType,
          cuisineStyle = "Personalized Clinical / MNT",
          defaultServings = if (item.servings > 0) item.servings else 1,
          ingredients = ingredientsList,
          preparationSteps = if (item.preparationSteps.isNotEmpty()) item.preparationSteps else listOf("Prepare wholesome ingredients and cook gently according to MNT guidelines."),
          calculatedNutrientsPerServing = calculatedNutrients,
          suitabilityNotes = item.suitabilityNotes.ifBlank { "Targeted for your chronic condition management." }
        )

        // Post-generation clinical safety validation (Independent Safety Gate)
        val validation = MealSafetyValidator.validateRecipe(recipe, profile)
        val adime = MealSafetyValidator.generateADIMERecord(recipe, profile)

        MealRecommendationEngine.RecommendedMealCandidate(
          recipe = recipe,
          validationResult = validation,
          adimeRecord = adime,
          isSuggestedForCurrentTime = (mealType.equals(MealRecommendationEngine.getSuggestedMealTypeForCurrentTime(), ignoreCase = true)),
          isAiGenerated = true,
          clinicalRationale = item.clinicalRationale.ifBlank {
            "Formulated to align with your clinical targets (${profile.medicalConditions.joinToString(", ").ifBlank { "General Health" }}): balance macronutrients, moderate sodium, and support metabolic stability."
          },
          adherenceTips = if (item.adherenceTips.isNotEmpty()) item.adherenceTips else listOf(
            "Batch prepare staple ingredients ahead of time.",
            "Use citrus and fresh herbs instead of added table salt to enhance flavor."
          )
        )
      }

      GenerationResult.Success(candidates = candidates, isAiGenerated = true)
    } catch (e: Exception) {
      Log.e("GeminiMealRepository", "Gemini generation failed: ${e.message}. Executing deterministic fallback.", e)
      val fallbackCandidates = MealRecommendationEngine.generateMealsForType(mealType, profile)
      GenerationResult.Fallback(
        candidates = fallbackCandidates,
        fallbackReason = "Live connection unavailable (${e.localizedMessage ?: "Network/Timeout"}). Showing clinical catalog recommendations."
      )
    }
  }

  private fun buildClinicalPrompt(
    mealType: String,
    profile: HealthProfile,
    customPrompt: String?
  ): String {
    val conditions = profile.medicalConditions.joinToString(", ").ifBlank { "General Health & Preventive Nutrition" }
    val medications = profile.medications.joinToString(", ").ifBlank { "None reported" }
    val dietPref = buildList {
      if (profile.dietaryPreference.isNotBlank()) add(profile.dietaryPreference)
      addAll(profile.dietaryRestrictions)
    }.joinToString(", ").ifBlank { "Balanced Standard" }
    val allergies = profile.allergies.joinToString(", ").ifBlank { "None" }
    val excluded = profile.excludedIngredients.joinToString(", ").ifBlank { "None" }

    return """
      You are an expert Clinical Dietitian and AI Nutritionist specializing in Medical Nutrition Therapy (MNT).
      Your base research is: "Personalized Meal Plan Generator Leveraging Large Language Models to Enhance Dietary and Nutritional Adherence".

      PATIENT CLINICAL PROFILE:
      - Age: ${profile.age} years | Biological Sex / Gender: ${profile.gender}
      - Weight: ${profile.weightKg} kg | Height: ${profile.heightCm} cm
      - Chronic Conditions: $conditions
      - Active Medications: $medications
      - Daily Target Calories: ${profile.dailyCalorieTarget} kcal
      - Target Macros: Protein: ${profile.proteinTargetG}g, Carbs: ${profile.carbsTargetG}g, Fat: ${profile.fatTargetG}g
      - Sodium Ceiling: ${profile.maxSodiumMg} mg/day | Potassium Limit: ${profile.maxPotassiumMg} mg/day
      - Dietary Preferences & Restrictions: $dietPref
      - Known Food Allergies (CRITICAL MUST EXCLUDE): $allergies
      - User Excluded Ingredients: $excluded

      TASK:
      Generate 3 distinct, delicious, and culturally appealing $mealType recipe candidates tailored specifically for this patient.
      ${if (!customPrompt.isNullOrBlank()) "Special patient request / craving: \"$customPrompt\"." else ""}

      MANDATORY CLINICAL GUARDRAILS:
      1. DRUG-NUTRIENT INTERACTIONS (DNI):
         - If on Warfarin: Maintain strictly consistent, moderate Vitamin K; avoid massive boluses of dark leafy greens.
         - If on Spironolactone/ACE-inhibitors: Restrict high-potassium foods (avoid salt substitutes with KCl, excess bananas/potatoes).
         - If on Statins: Strictly exclude grapefruit / Seville oranges.
      2. DISEASE SPECIFIC CONTROLS:
         - Type 2 Diabetes: Low Glycemic Index carbs, high soluble fiber (>6g per meal), zero refined sugars.
         - Hypertension: Low sodium (<450mg per meal), rich in natural bioavailable minerals.
         - Chronic Kidney Disease (CKD): Moderate/controlled high-biological-value protein, monitor sodium and potassium.
      3. ADHERENCE EMPOWERMENT:
         - Provide a clear, empowering "clinicalRationale" explaining WHY this specific meal directly benefits their chronic condition and stabilizes their biomarkers.
         - Provide 2-3 practical "adherenceTips" (e.g. smart flavor substitution, meal prep time-savers) to maximize long-term compliance.

      Return ONLY a valid JSON object matching this exact structure:
      {
        "candidates": [
          {
            "title": "Recipe Title",
            "description": "Appetizing 1-sentence description.",
            "mealType": "$mealType",
            "prepTimeMinutes": 20,
            "servings": 1,
            "calories": 420.0,
            "proteinGrams": 24.0,
            "carbsGrams": 50.0,
            "fatGrams": 12.0,
            "fiberGrams": 8.0,
            "sodiumMg": 340.0,
            "potassiumMg": 420.0,
            "ingredients": [
              {
                "displayName": "Ingredient Name",
                "quantityGrams": 100.0,
                "displayUnit": "100g",
                "preparationNote": "chopped or steamed"
              }
            ],
            "preparationSteps": [
              "Step 1...",
              "Step 2..."
            ],
            "clinicalRationale": "Detailed physiological explanation: how the nutrients in this meal specifically manage the patient's conditions ($conditions) and medications ($medications).",
            "adherenceTips": [
              "Actionable tip to make preparation effortless or boost satiety...",
              "Culinary flavor enhancer tip without using sodium..."
            ],
            "suitabilityNotes": "High Fiber • Low Glycemic Index • Controlled Sodium"
          }
        ]
      }
    """.trimIndent()
  }
}
