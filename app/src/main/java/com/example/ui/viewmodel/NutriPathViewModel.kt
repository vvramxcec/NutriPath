package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.NutriPathDatabase
import com.example.data.engine.MealRecommendationEngine
import com.example.data.model.FoodIngredient
import com.example.data.model.HealthProfile
import com.example.data.model.MealPlanEntity
import com.example.data.repository.FoodCompositionRepository
import com.example.data.repository.HealthProfileRepository
import com.example.data.repository.MealPlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.content.Context
import com.example.data.repository.AuthRepository
import com.example.data.repository.CloudSyncRepository
import com.example.data.repository.GeminiMealRepository
import com.google.firebase.auth.FirebaseUser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class NutriPathViewModel(application: Application) : AndroidViewModel(application) {

  enum class MealGenerationMode {
    GEMINI_AI,
    DETERMINISTIC_CATALOG
  }

  private val database = NutriPathDatabase.getDatabase(application)
  private val healthProfileRepo = HealthProfileRepository(database.healthProfileDao())
  private val mealPlanRepo = MealPlanRepository(database.mealPlanDao())
  private val authRepo = AuthRepository()
  private val cloudSyncRepo = CloudSyncRepository(application)
  private val geminiMealRepo = GeminiMealRepository()

  val currentUser: StateFlow<FirebaseUser?> = authRepo.authStateFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, authRepo.currentUser)

  private val _isAuthenticating = MutableStateFlow(false)
  val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

  val healthProfile: StateFlow<HealthProfile> = healthProfileRepo.profileFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, HealthProfile())

  private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
  private val displayFormat = SimpleDateFormat("EEEE, MMM d", Locale.getDefault())

  private val _selectedDate = MutableStateFlow(dateFormat.format(Date()))
  val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

  val selectedDateDisplay: StateFlow<String> = _selectedDate.combine(MutableStateFlow(Unit)) { dateStr, _ ->
    try {
      val parsed = dateFormat.parse(dateStr)
      if (parsed != null) displayFormat.format(parsed) else dateStr
    } catch (e: Exception) {
      dateStr
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Today")

  // Current date's meals
  val currentMeals: StateFlow<List<MealPlanEntity>> = _selectedDate
    .flatMapLatest { date -> mealPlanRepo.getMealsForDate(date) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // All-time meal history
  val mealHistory: StateFlow<List<MealPlanEntity>> = mealPlanRepo.getAllConsumedHistory()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Consumed totals for selected date (Only meals marked 'CONSUMED')
  val consumedCalories: StateFlow<Float> = currentMeals.combine(MutableStateFlow(Unit)) { meals, _ ->
    meals.filter { it.status == MealPlanEntity.STATUS_CONSUMED }.sumOf { it.caloriesKcal.toDouble() }.toFloat()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  val consumedProtein: StateFlow<Float> = currentMeals.combine(MutableStateFlow(Unit)) { meals, _ ->
    meals.filter { it.status == MealPlanEntity.STATUS_CONSUMED }.sumOf { it.proteinGrams.toDouble() }.toFloat()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  val consumedCarbs: StateFlow<Float> = currentMeals.combine(MutableStateFlow(Unit)) { meals, _ ->
    meals.filter { it.status == MealPlanEntity.STATUS_CONSUMED }.sumOf { it.carbsGrams.toDouble() }.toFloat()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  val consumedFat: StateFlow<Float> = currentMeals.combine(MutableStateFlow(Unit)) { meals, _ ->
    meals.filter { it.status == MealPlanEntity.STATUS_CONSUMED }.sumOf { it.fatGrams.toDouble() }.toFloat()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  val consumedFiber: StateFlow<Float> = currentMeals.combine(MutableStateFlow(Unit)) { meals, _ ->
    meals.filter { it.status == MealPlanEntity.STATUS_CONSUMED }.sumOf { it.fiberGrams.toDouble() }.toFloat()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  val consumedSodium: StateFlow<Float> = currentMeals.combine(MutableStateFlow(Unit)) { meals, _ ->
    meals.filter { it.status == MealPlanEntity.STATUS_CONSUMED }.sumOf { it.sodiumMg.toDouble() }.toFloat()
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

  // Meal Generator State
  private val _selectedGenerationType = MutableStateFlow(MealRecommendationEngine.getSuggestedMealTypeForCurrentTime())
  val selectedGenerationType: StateFlow<String> = _selectedGenerationType.asStateFlow()

  private val _generatedCandidates = MutableStateFlow<List<MealRecommendationEngine.RecommendedMealCandidate>>(emptyList())
  val generatedCandidates: StateFlow<List<MealRecommendationEngine.RecommendedMealCandidate>> = _generatedCandidates.asStateFlow()

  private val _fullDayPlan = MutableStateFlow<MealRecommendationEngine.FullDayMealPlan?>(null)
  val fullDayPlan: StateFlow<MealRecommendationEngine.FullDayMealPlan?> = _fullDayPlan.asStateFlow()

  private val _isGenerating = MutableStateFlow(false)
  val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

  private val _generationMode = MutableStateFlow(MealGenerationMode.GEMINI_AI)
  val generationMode: StateFlow<MealGenerationMode> = _generationMode.asStateFlow()

  private val _fallbackNotice = MutableStateFlow<String?>(null)
  val fallbackNotice: StateFlow<String?> = _fallbackNotice.asStateFlow()

  private val _isOnboardingActive = MutableStateFlow(true)
  val isOnboardingActive: StateFlow<Boolean> = _isOnboardingActive.asStateFlow()

  // Anuvaad Dataset count state
  private val _anuvaadDatasetCount = MutableStateFlow(FoodCompositionRepository.getAllIngredients().size)
  val anuvaadDatasetCount: StateFlow<Int> = _anuvaadDatasetCount.asStateFlow()

  init {
    viewModelScope.launch {
      NutriPathDatabase.seedIndianFoodDatasetIfEmpty(application, database)
      val indianFoods = database.indianFoodDao().getAllFoodsOnce()
      FoodCompositionRepository.registerIndianEntities(indianFoods)
      _anuvaadDatasetCount.value = FoodCompositionRepository.getAllIngredients().size

      val existing = database.healthProfileDao().getHealthProfileOnce()
      if (existing == null || !existing.hasCompletedOnboarding) {
        val initialProfile = existing ?: HealthProfile(hasCompletedOnboarding = false)
        healthProfileRepo.saveProfile(initialProfile)
        _isOnboardingActive.value = true
      } else {
        _isOnboardingActive.value = false
      }

      authRepo.currentUser?.let { user ->
        restoreUserDataFromCloud(user.uid)
      }
    }
  }

  private suspend fun restoreUserDataFromCloud(userId: String) {
    try {
      val cloudProfileRes = cloudSyncRepo.fetchProfileFromCloud(userId)
      val cloudProfile = cloudProfileRes.getOrNull()
      if (cloudProfile != null && cloudProfile.hasCompletedOnboarding) {
        healthProfileRepo.saveProfile(cloudProfile)
        _isOnboardingActive.value = false
      }

      val cloudMealsRes = cloudSyncRepo.fetchMealPlansFromCloud(userId)
      val cloudMeals = cloudMealsRes.getOrNull() ?: emptyList()
      if (cloudMeals.isNotEmpty()) {
        mealPlanRepo.insertMeals(cloudMeals)
      }
    } catch (_: Exception) {
      // Graceful offline fallback
    }
  }

  private suspend fun seedInitialMealsIfEmpty(today: String) {
    val existing = mealPlanRepo.getMealsForDateOnce(today)
    if (existing.isNotEmpty()) return
    val initialProfile = healthProfileRepo.getProfileOnce()
    val fullPlan = MealRecommendationEngine.generateFullDayPlan(today, initialProfile)

    val breakfast = MealPlanEntity(
      date = today,
      mealType = "Breakfast",
      recipeId = fullPlan.breakfast.recipe.id,
      recipeTitle = fullPlan.breakfast.recipe.title,
      servings = 1.0f,
      caloriesKcal = fullPlan.breakfast.recipe.calculatedNutrientsPerServing.caloriesKcal,
      proteinGrams = fullPlan.breakfast.recipe.calculatedNutrientsPerServing.proteinGrams,
      carbsGrams = fullPlan.breakfast.recipe.calculatedNutrientsPerServing.carbsGrams,
      fatGrams = fullPlan.breakfast.recipe.calculatedNutrientsPerServing.fatGrams,
      fiberGrams = fullPlan.breakfast.recipe.calculatedNutrientsPerServing.fiberGrams,
      sodiumMg = fullPlan.breakfast.recipe.calculatedNutrientsPerServing.sodiumMg,
      status = MealPlanEntity.STATUS_PLANNED,
      validationStatus = fullPlan.breakfast.validationResult.overallStatus.name,
      suitabilityExplanation = fullPlan.breakfast.validationResult.suitabilityExplanation,
      ingredientsSummary = fullPlan.breakfast.recipe.ingredients.joinToString(", ") { "${it.displayName} (${it.quantityGrams.toInt()}g)" },
      instructionsSummary = fullPlan.breakfast.recipe.preparationSteps.joinToString(". ")
    )

    val lunch = MealPlanEntity(
      date = today,
      mealType = "Lunch",
      recipeId = fullPlan.lunch.recipe.id,
      recipeTitle = fullPlan.lunch.recipe.title,
      servings = 1.0f,
      caloriesKcal = fullPlan.lunch.recipe.calculatedNutrientsPerServing.caloriesKcal,
      proteinGrams = fullPlan.lunch.recipe.calculatedNutrientsPerServing.proteinGrams,
      carbsGrams = fullPlan.lunch.recipe.calculatedNutrientsPerServing.carbsGrams,
      fatGrams = fullPlan.lunch.recipe.calculatedNutrientsPerServing.fatGrams,
      fiberGrams = fullPlan.lunch.recipe.calculatedNutrientsPerServing.fiberGrams,
      sodiumMg = fullPlan.lunch.recipe.calculatedNutrientsPerServing.sodiumMg,
      status = MealPlanEntity.STATUS_PLANNED,
      validationStatus = fullPlan.lunch.validationResult.overallStatus.name,
      suitabilityExplanation = fullPlan.lunch.validationResult.suitabilityExplanation,
      ingredientsSummary = fullPlan.lunch.recipe.ingredients.joinToString(", ") { "${it.displayName} (${it.quantityGrams.toInt()}g)" },
      instructionsSummary = fullPlan.lunch.recipe.preparationSteps.joinToString(". ")
    )

    val dinner = MealPlanEntity(
      date = today,
      mealType = "Dinner",
      recipeId = fullPlan.dinner.recipe.id,
      recipeTitle = fullPlan.dinner.recipe.title,
      servings = 1.0f,
      caloriesKcal = fullPlan.dinner.recipe.calculatedNutrientsPerServing.caloriesKcal,
      proteinGrams = fullPlan.dinner.recipe.calculatedNutrientsPerServing.proteinGrams,
      carbsGrams = fullPlan.dinner.recipe.calculatedNutrientsPerServing.carbsGrams,
      fatGrams = fullPlan.dinner.recipe.calculatedNutrientsPerServing.fatGrams,
      fiberGrams = fullPlan.dinner.recipe.calculatedNutrientsPerServing.fiberGrams,
      sodiumMg = fullPlan.dinner.recipe.calculatedNutrientsPerServing.sodiumMg,
      status = MealPlanEntity.STATUS_PLANNED,
      validationStatus = fullPlan.dinner.validationResult.overallStatus.name,
      suitabilityExplanation = fullPlan.dinner.validationResult.suitabilityExplanation,
      ingredientsSummary = fullPlan.dinner.recipe.ingredients.joinToString(", ") { "${it.displayName} (${it.quantityGrams.toInt()}g)" },
      instructionsSummary = fullPlan.dinner.recipe.preparationSteps.joinToString(". ")
    )

    mealPlanRepo.insertMeals(listOf(breakfast, lunch, dinner))
  }

  fun generateAndApplyFullDayPlanForSelectedDate(onComplete: () -> Unit = {}) {
    viewModelScope.launch {
      _isGenerating.value = true
      val profile = healthProfile.value
      val targetDate = _selectedDate.value
      val plan = MealRecommendationEngine.generateFullDayPlan(targetDate, profile)
      _fullDayPlan.value = plan

      // Clear existing unconsumed planned meals for this date so they are replaced by the fresh tailored plan,
      // while keeping any meals the user already marked as consumed intact!
      mealPlanRepo.deletePlannedMealsForDate(targetDate)

      val items = listOf(
        plan.breakfast to "Breakfast",
        plan.lunch to "Lunch",
        plan.dinner to "Dinner",
        plan.snack to "Snack"
      ).map { (candidate, type) ->
        val recipe = candidate.recipe
        MealPlanEntity(
          date = targetDate,
          mealType = type,
          recipeId = recipe.id,
          recipeTitle = recipe.title,
          servings = 1.0f,
          caloriesKcal = recipe.calculatedNutrientsPerServing.caloriesKcal,
          proteinGrams = recipe.calculatedNutrientsPerServing.proteinGrams,
          carbsGrams = recipe.calculatedNutrientsPerServing.carbsGrams,
          fatGrams = recipe.calculatedNutrientsPerServing.fatGrams,
          fiberGrams = recipe.calculatedNutrientsPerServing.fiberGrams,
          sodiumMg = recipe.calculatedNutrientsPerServing.sodiumMg,
          status = MealPlanEntity.STATUS_PLANNED,
          validationStatus = candidate.validationResult.overallStatus.name,
          suitabilityExplanation = candidate.validationResult.suitabilityExplanation,
          ingredientsSummary = recipe.ingredients.joinToString(", ") { "${it.displayName} (${it.quantityGrams.toInt()}g)" },
          instructionsSummary = recipe.preparationSteps.joinToString(". ")
        )
      }
      mealPlanRepo.insertMeals(items)
      authRepo.currentUser?.let { user ->
        cloudSyncRepo.syncMealPlansToCloud(user.uid, items)
      }
      _isGenerating.value = false
      onComplete()
    }
  }

  fun changeSelectedDate(offsetDays: Int) {
    try {
      val cal = Calendar.getInstance()
      val current = dateFormat.parse(_selectedDate.value)
      if (current != null) cal.time = current
      cal.add(Calendar.DAY_OF_YEAR, offsetDays)
      _selectedDate.value = dateFormat.format(cal.time)
    } catch (e: Exception) {
      _selectedDate.value = dateFormat.format(Date())
    }
  }

  fun setSelectedDate(dateString: String) {
    _selectedDate.value = dateString
  }

  fun setGenerationMealType(type: String) {
    _selectedGenerationType.value = type
    generateRecommendations(type)
  }

  fun setGenerationMode(mode: MealGenerationMode) {
    _generationMode.value = mode
    generateRecommendations(_selectedGenerationType.value)
  }

  fun clearFallbackNotice() {
    _fallbackNotice.value = null
  }

  fun generateRecommendations(
    mealType: String = _selectedGenerationType.value,
    customPrompt: String? = null
  ) {
    viewModelScope.launch {
      _isGenerating.value = true
      _fallbackNotice.value = null
      val profile = healthProfile.value
      if (mealType.equals("Full-Day Plan", ignoreCase = true)) {
        val plan = MealRecommendationEngine.generateFullDayPlan(_selectedDate.value, profile)
        _fullDayPlan.value = plan
        _generatedCandidates.value = emptyList()
      } else {
        if (_generationMode.value == MealGenerationMode.GEMINI_AI) {
          when (val result = geminiMealRepo.generatePersonalizedMeals(mealType, profile, customPrompt)) {
            is GeminiMealRepository.GenerationResult.Success -> {
              _generatedCandidates.value = result.candidates
              _fallbackNotice.value = null
              _fullDayPlan.value = null
            }
            is GeminiMealRepository.GenerationResult.Fallback -> {
              _generatedCandidates.value = result.candidates
              _fallbackNotice.value = result.fallbackReason
              _fullDayPlan.value = null
            }
          }
        } else {
          val list = MealRecommendationEngine.generateMealsForType(mealType, profile)
          _generatedCandidates.value = list
          _fullDayPlan.value = null
        }
      }
      _isGenerating.value = false
    }
  }

  /**
   * HealthGenie Circular Interactive Adaptation:
   * Dynamically adapts the candidate recipe when user clicks "Exclude" on an ingredient.
   */
  fun adaptCandidateWithExclusion(candidateIndex: Int, excludedIngredientId: String) {
    val currentList = _generatedCandidates.value
    if (candidateIndex in currentList.indices) {
      val target = currentList[candidateIndex]
      val adapted = MealRecommendationEngine.adaptRecipeWithExclusion(target, excludedIngredientId, healthProfile.value)
      val updatedList = currentList.toMutableList()
      updatedList[candidateIndex] = adapted
      _generatedCandidates.value = updatedList
    }
  }

  /**
   * HealthGenie Persistent Ingredient Exclusion:
   * Permanently adds an ingredient to user's profile exclusions and updates database.
   */
  fun togglePersistentExclusion(ingredientName: String) {
    viewModelScope.launch {
      val profile = healthProfile.value
      val currentExclusions = profile.excludedIngredients.toMutableList()
      if (currentExclusions.contains(ingredientName)) {
        currentExclusions.remove(ingredientName)
      } else {
        currentExclusions.add(ingredientName)
      }
      val updated = profile.copy(excludedIngredients = currentExclusions)
      healthProfileRepo.saveProfile(updated)
      generateRecommendations()
    }
  }

  /**
   * Ingests CSV dataset from Anuvaad into the runtime food composition registry.
   */
  fun ingestAnuvaadCsv(csvContent: String): FoodCompositionRepository.IngestionSummary {
    val summary = FoodCompositionRepository.importFromCsv(csvContent)
    _anuvaadDatasetCount.value = FoodCompositionRepository.getAllIngredients().size
    // Regenerate recommendations with enriched dataset
    generateRecommendations()
    return summary
  }

  fun searchAnuvaadFoods(query: String, category: String? = null): List<FoodIngredient> {
    return FoodCompositionRepository.searchIngredients(query, category)
  }

  fun saveCandidateToPlan(candidate: MealRecommendationEngine.RecommendedMealCandidate, targetDate: String = _selectedDate.value, asConsumed: Boolean = false) {
    viewModelScope.launch {
      val recipe = candidate.recipe
      val entity = MealPlanEntity(
        date = targetDate,
        mealType = recipe.mealType,
        recipeId = recipe.id,
        recipeTitle = recipe.title,
        servings = 1.0f,
        caloriesKcal = recipe.calculatedNutrientsPerServing.caloriesKcal,
        proteinGrams = recipe.calculatedNutrientsPerServing.proteinGrams,
        carbsGrams = recipe.calculatedNutrientsPerServing.carbsGrams,
        fatGrams = recipe.calculatedNutrientsPerServing.fatGrams,
        fiberGrams = recipe.calculatedNutrientsPerServing.fiberGrams,
        sodiumMg = recipe.calculatedNutrientsPerServing.sodiumMg,
        status = if (asConsumed) MealPlanEntity.STATUS_CONSUMED else MealPlanEntity.STATUS_PLANNED,
        validationStatus = candidate.validationResult.overallStatus.name,
        suitabilityExplanation = candidate.validationResult.suitabilityExplanation,
        ingredientsSummary = recipe.ingredients.joinToString(", ") { "${it.displayName} (${it.quantityGrams.toInt()}g)" },
        instructionsSummary = recipe.preparationSteps.joinToString(". ")
      )
      mealPlanRepo.insertMeal(entity)
    }
  }

  fun saveFullDayPlanToDate(plan: MealRecommendationEngine.FullDayMealPlan, targetDate: String = _selectedDate.value) {
    viewModelScope.launch {
      val items = listOf(
        plan.breakfast to "Breakfast",
        plan.lunch to "Lunch",
        plan.dinner to "Dinner",
        plan.snack to "Snack"
      ).map { (cand, type) ->
        val r = cand.recipe
        MealPlanEntity(
          date = targetDate,
          mealType = type,
          recipeId = r.id,
          recipeTitle = r.title,
          servings = 1.0f,
          caloriesKcal = r.calculatedNutrientsPerServing.caloriesKcal,
          proteinGrams = r.calculatedNutrientsPerServing.proteinGrams,
          carbsGrams = r.calculatedNutrientsPerServing.carbsGrams,
          fatGrams = r.calculatedNutrientsPerServing.fatGrams,
          fiberGrams = r.calculatedNutrientsPerServing.fiberGrams,
          sodiumMg = r.calculatedNutrientsPerServing.sodiumMg,
          status = MealPlanEntity.STATUS_PLANNED,
          validationStatus = cand.validationResult.overallStatus.name,
          suitabilityExplanation = cand.validationResult.suitabilityExplanation,
          ingredientsSummary = r.ingredients.joinToString(", ") { "${it.displayName} (${it.quantityGrams.toInt()}g)" },
          instructionsSummary = r.preparationSteps.joinToString(". ")
        )
      }
      mealPlanRepo.insertMeals(items)
    }
  }

  fun markMealConsumed(id: Long) {
    viewModelScope.launch {
      mealPlanRepo.markMealConsumed(id)
    }
  }

  fun markMealPlanned(id: Long) {
    viewModelScope.launch {
      mealPlanRepo.markMealPlanned(id)
    }
  }

  fun updateMealServings(meal: MealPlanEntity, newServings: Float) {
    viewModelScope.launch {
      val singleCal = meal.caloriesKcal / meal.servings
      val singlePro = meal.proteinGrams / meal.servings
      val singleCarb = meal.carbsGrams / meal.servings
      val singleFat = meal.fatGrams / meal.servings
      val singleSod = meal.sodiumMg / meal.servings
      mealPlanRepo.updateServings(meal.id, newServings, singleCal, singlePro, singleCarb, singleFat, singleSod)
    }
  }

  fun deleteMeal(id: Long) {
    viewModelScope.launch {
      mealPlanRepo.deleteMeal(id)
    }
  }

  fun saveProfile(profile: HealthProfile) {
    viewModelScope.launch {
      healthProfileRepo.saveProfile(profile)
      generateRecommendations(_selectedGenerationType.value)
    }
  }

  fun completeOnboarding(profile: HealthProfile) {
    viewModelScope.launch {
      val finished = profile.copy(hasCompletedOnboarding = true)
      healthProfileRepo.saveProfile(finished)
      _isOnboardingActive.value = false
      generateRecommendations()
    }
  }

  fun restartOnboarding() {
    _isOnboardingActive.value = true
  }

  fun dismissOnboarding() {
    // Only dismiss if the user has genuinely completed their clinical profile details
    if (healthProfile.value.hasCompletedOnboarding) {
      _isOnboardingActive.value = false
    }
  }

  fun clearAllData() {
    viewModelScope.launch {
      mealPlanRepo.clearAll()
      healthProfileRepo.resetProfile()
      _isOnboardingActive.value = true
    }
  }

  fun signInWithGoogle(
    context: Context,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _isAuthenticating.value = true
      val result = authRepo.signInWithGoogleCredential(context)
      _isAuthenticating.value = false
      result.fold(
        onSuccess = { user ->
          val extractedName = user.displayName?.trim()?.ifBlank { null }
            ?: user.email?.substringBefore("@")?.replace(".", " ")?.replace("_", " ")
              ?.split(" ")?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
            ?: ""

          val current = healthProfile.value
          val updated = if (extractedName.isNotBlank() && current.name.isBlank()) {
            current.copy(name = extractedName)
          } else if (extractedName.isNotBlank()) {
            current.copy(name = extractedName)
          } else {
            current
          }
          healthProfileRepo.saveProfile(updated)
          cloudSyncRepo.syncProfileToCloud(user.uid, updated)
          onSuccess()
        },
        onFailure = { error ->
          onError(error.localizedMessage ?: "Google sign in failed")
        }
      )
    }
  }

  fun signInWithEmail(
    email: String,
    pass: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _isAuthenticating.value = true
      val result = authRepo.signInWithEmail(email, pass)
      _isAuthenticating.value = false
      result.fold(
        onSuccess = { user ->
          cloudSyncRepo.syncProfileToCloud(user.uid, healthProfile.value)
          onSuccess()
        },
        onFailure = { error ->
          onError(error.localizedMessage ?: "Invalid email or password")
        }
      )
    }
  }

  fun signUpWithEmail(
    email: String,
    pass: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _isAuthenticating.value = true
      val result = authRepo.signUpWithEmail(email, pass)
      _isAuthenticating.value = false
      result.fold(
        onSuccess = { user ->
          cloudSyncRepo.syncProfileToCloud(user.uid, healthProfile.value)
          onSuccess()
        },
        onFailure = { error ->
          onError(error.localizedMessage ?: "Failed to create account")
        }
      )
    }
  }

  fun signOut() {
    authRepo.signOut()
  }

  fun deleteAccountAndPurgeData(
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      _isAuthenticating.value = true
      val uid = authRepo.currentUser?.uid
      if (uid != null) {
        // 1. Delete cloud profile in Firestore
        cloudSyncRepo.deleteUserDataFromCloud(uid)
        // 2. Delete auth user identity in Firebase Auth
        val deleteResult = authRepo.deleteAccount()
        deleteResult.fold(
          onSuccess = {
            // 3. Purge local Room SQLite database & restart onboarding
            clearAllData()
            _isAuthenticating.value = false
            onSuccess()
          },
          onFailure = { error ->
            _isAuthenticating.value = false
            onError(error.localizedMessage ?: "Failed to delete account. Please re-authenticate and try again.")
          }
        )
      } else {
        // Guest mode purge
        clearAllData()
        _isAuthenticating.value = false
        onSuccess()
      }
    }
  }
}

