package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.engine.MealRecommendationEngine
import com.example.data.model.ADIMEClinicalRecord
import com.example.data.model.ValidationStatus
import com.example.ui.components.ADIMEClinicalDialog
import com.example.ui.components.ValidationBadge
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowAccent
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.MacroCarbsText
import com.example.ui.theme.MacroFatText
import com.example.ui.theme.MacroProteinText
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard
import com.example.ui.viewmodel.NutriPathViewModel
import kotlinx.coroutines.launch

@Composable
fun MealGeneratorScreen(
  viewModel: NutriPathViewModel,
  initialMealType: String? = null,
  onNavigateBack: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val profile by viewModel.healthProfile.collectAsStateWithLifecycle()
  val selectedType by viewModel.selectedGenerationType.collectAsStateWithLifecycle()
  val candidates by viewModel.generatedCandidates.collectAsStateWithLifecycle()
  val fullDayPlan by viewModel.fullDayPlan.collectAsStateWithLifecycle()
  val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
  val selectedDateDisplay by viewModel.selectedDateDisplay.collectAsStateWithLifecycle()

  val suggestedTimeType = remember { MealRecommendationEngine.getSuggestedMealTypeForCurrentTime() }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  var inspectedAdimeRecord by remember { mutableStateOf<ADIMEClinicalRecord?>(null) }

  val mealTypeOptions = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Full-Day Plan")

  LaunchedEffect(initialMealType) {
    if (initialMealType != null) {
      viewModel.setGenerationMealType(initialMealType)
    } else if (candidates.isEmpty() && fullDayPlan == null) {
      viewModel.generateRecommendations(selectedType)
    }
  }

  if (inspectedAdimeRecord != null) {
    ADIMEClinicalDialog(
      adimeRecord = inspectedAdimeRecord!!,
      onDismiss = { inspectedAdimeRecord = null }
    )
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(LimeYellowBackground)
        .testTag("meal_generator_lazy_column"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Time-Aware Suggestion Banner
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(LimeYellowBackground),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = DeepTealPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Time-Aware Recommendation (PRD 4.4)",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "Smart suggestion for right now: $suggestedTimeType",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTealDark
              )
            }
          }
        }
      }

      // 2. Meal Type Selector Tabs
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          mealTypeOptions.forEach { type ->
            val isSelected = selectedType.equals(type, ignoreCase = true)
            val isSuggested = type.equals(suggestedTimeType, ignoreCase = true)

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (isSelected) DeepTealPrimary else WarmWhiteCard)
                .border(
                  1.dp,
                  if (isSelected) DeepTealDark else BorderSubtle,
                  RoundedCornerShape(12.dp)
                )
                .clickable { viewModel.setGenerationMealType(type) }
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("meal_type_tab_${type.lowercase().replace(" ", "_")}")
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = type,
                  fontSize = 13.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) Color.White else DarkCharcoalText
                )
                if (isSuggested) {
                  Spacer(modifier = Modifier.width(4.dp))
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(if (isSelected) LimeYellowAccent else DeepTealPrimary)
                  )
                }
              }
            }
          }
        }
      }

      // 3. User Health Profile Summary Pill with Pharmacological Safe Gate Status
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFE7EFC9)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.HealthAndSafety,
                  contentDescription = null,
                  tint = DeepTealDark,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Active Clinical Profile & DNI Safety Gate:",
                  fontSize = 11.sp,
                  color = DeepTealDark,
                  fontWeight = FontWeight.Bold
                )
              }
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${profile.medicalConditions.joinToString(", ")} • Meds: ${if (profile.medications.isNotEmpty()) profile.medications.joinToString(", ") else "None"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = DarkCharcoalText
              )
              if (profile.excludedIngredients.isNotEmpty()) {
                Text(
                  text = "User Exclusions: ${profile.excludedIngredients.joinToString(", ")}",
                  fontSize = 10.sp,
                  color = StatusRejectedRed,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            IconButton(
              onClick = { viewModel.generateRecommendations() },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Regenerate",
                tint = DeepTealDark
              )
            }
          }
        }
      }

      // 4. Loading State
      if (isGenerating) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator(color = DeepTealPrimary)
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Applying NutriOrion Clinical Gates & Anuvaad Calculations...",
                fontSize = 13.sp,
                color = DarkCharcoalText,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      // 5. Full-Day Plan View
      if (fullDayPlan != null && !isGenerating) {
        val plan = fullDayPlan!!
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = "Complete Full-Day Plan for $selectedDateDisplay",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoalText
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = plan.planSuitabilityNote,
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 16.sp
              )
              Spacer(modifier = Modifier.height(12.dp))

              // Daily Totals Row
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                NutrientMiniBox("Calories", "${plan.totalCalories.toInt()} kcal", DeepTealPrimary)
                NutrientMiniBox("Protein", "${plan.totalProtein.toInt()}g", MacroProteinText)
                NutrientMiniBox("Carbs", "${plan.totalCarbs.toInt()}g", MacroCarbsText)
                NutrientMiniBox("Fat", "${plan.totalFat.toInt()}g", MacroFatText)
              }

              Spacer(modifier = Modifier.height(14.dp))
              Button(
                onClick = {
                  viewModel.saveFullDayPlanToDate(plan)
                  scope.launch {
                    snackbarHostState.showSnackbar("Full-Day Plan successfully added to today's schedule!")
                  }
                },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("save_full_day_plan_button"),
                colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
                shape = RoundedCornerShape(12.dp)
              ) {
                Icon(imageVector = Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Schedule All 4 Meals to Plan", fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        // Display the 4 meals in the plan
        itemsIndexed(
          listOf(
            plan.breakfast to "Breakfast",
            plan.lunch to "Lunch",
            plan.dinner to "Dinner",
            plan.snack to "Snack"
          )
        ) { index, (candidate, type) ->
          CandidateRecipeCard(
            candidate = candidate,
            index = index,
            onExcludeIngredient = { ingId ->
              viewModel.adaptCandidateWithExclusion(index, ingId)
              scope.launch { snackbarHostState.showSnackbar("Ingredient excluded. Adapted recipe with safe alternative!") }
            },
            onInspectAdime = { inspectedAdimeRecord = candidate.adimeRecord },
            onAddToPlan = {
              viewModel.saveCandidateToPlan(candidate, asConsumed = false)
              scope.launch { snackbarHostState.showSnackbar("Added $type to Plan!") }
            },
            onLogConsumed = {
              viewModel.saveCandidateToPlan(candidate, asConsumed = true)
              scope.launch { snackbarHostState.showSnackbar("Logged $type as Consumed!") }
            }
          )
        }
      }

      // 6. Individual 3 Meal Candidates List (NutriGen Diverse Options)
      if (candidates.isNotEmpty() && !isGenerating && fullDayPlan == null) {
        itemsIndexed(candidates) { index, candidate ->
          CandidateRecipeCard(
            candidate = candidate,
            index = index,
            onExcludeIngredient = { ingId ->
              viewModel.adaptCandidateWithExclusion(index, ingId)
              scope.launch { snackbarHostState.showSnackbar("Ingredient excluded. Adapted recipe dynamically!") }
            },
            onInspectAdime = { inspectedAdimeRecord = candidate.adimeRecord },
            onAddToPlan = {
              viewModel.saveCandidateToPlan(candidate, asConsumed = false)
              scope.launch { snackbarHostState.showSnackbar("Added '${candidate.recipe.title}' to Plan!") }
            },
            onLogConsumed = {
              viewModel.saveCandidateToPlan(candidate, asConsumed = true)
              scope.launch { snackbarHostState.showSnackbar("Logged '${candidate.recipe.title}' as Consumed!") }
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }

    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 16.dp)
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CandidateRecipeCard(
  candidate: MealRecommendationEngine.RecommendedMealCandidate,
  index: Int,
  onExcludeIngredient: (String) -> Unit,
  onInspectAdime: () -> Unit,
  onAddToPlan: () -> Unit,
  onLogConsumed: () -> Unit,
  modifier: Modifier = Modifier
) {
  val recipe = candidate.recipe
  val nutrients = recipe.calculatedNutrientsPerServing
  val validation = candidate.validationResult

  var isIngredientsExpanded by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("candidate_card_${recipe.id}"),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Header: Option Number & Cuisine Style
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(DeepTealPrimary)
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Option ${index + 1}",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = recipe.cuisineStyle,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepTealPrimary
          )
        }

        if (candidate.isSuggestedForCurrentTime) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(LimeYellowBackground)
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "Time Match",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = DeepTealDark
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Recipe Title & Description
      Text(
        text = recipe.title,
        fontSize = 17.sp,
        fontWeight = FontWeight.Bold,
        color = DarkCharcoalText
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = recipe.description,
        fontSize = 12.sp,
        color = TextMuted,
        lineHeight = 16.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      // HealthGenie Circular Interactive Ingredient Entity Chips (arXiv:2504.14594)
      Text(
        text = "Interactive Ingredients (Tap '×' to Exclude):",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = DarkCharcoalText
      )
      Spacer(modifier = Modifier.height(6.dp))
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        recipe.ingredients.forEach { ing ->
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (ing.isSubstituted) Color(0xFFE0F2FE) else Color(0xFFF3F6E6))
              .border(
                1.dp,
                if (ing.isSubstituted) Color(0xFF7DD3FC) else Color(0xFFD8E2B8),
                RoundedCornerShape(8.dp)
              )
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "${ing.displayName.split(" (")[0]} (${ing.quantityGrams.toInt()}g)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = DarkCharcoalText
              )
              Spacer(modifier = Modifier.width(4.dp))
              // Exclude button
              Box(
                modifier = Modifier
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE2E8D0))
                  .clickable { onExcludeIngredient(ing.ingredientId) },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Exclude ${ing.displayName}",
                  tint = StatusRejectedRed,
                  modifier = Modifier.size(10.dp)
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Deterministic Nutrients Grid
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        NutrientMiniBox("Calories", "${nutrients.caloriesKcal.toInt()} kcal", DeepTealPrimary)
        NutrientMiniBox("Protein", "${nutrients.proteinGrams.toInt()}g", MacroProteinText)
        NutrientMiniBox("Carbs", "${nutrients.carbsGrams.toInt()}g", MacroCarbsText)
        NutrientMiniBox("Fat", "${nutrients.fatGrams.toInt()}g", MacroFatText)
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Micronutrients Row (NutriOrion clinical monitoring)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFFF8F9F3))
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Na: ${nutrients.sodiumMg.toInt()}mg  •  K: ${nutrients.potassiumMg.toInt()}mg  •  Fiber: ${nutrients.fiberGrams.toInt()}g  •  Vit K: ${nutrients.vitaminKMcg.toInt()}µg",
          fontSize = 10.sp,
          fontWeight = FontWeight.SemiBold,
          color = DeepTealDark
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Independent Meal Safety Validator Badge with audit
      ValidationBadge(
        result = validation,
        expandable = true,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      // NutriOrion ADIME Review Button
      OutlinedButton(
        onClick = onInspectAdime,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, DeepTealPrimary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTealPrimary)
      ) {
        Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("NutriOrion Clinical Rationale (ADIME)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Preparation instructions toggle
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFFF9FAF6))
          .clickable { isIngredientsExpanded = !isIngredientsExpanded }
          .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Culinary Preparation Steps (${recipe.preparationSteps.size})",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = DarkCharcoalText
        )
        Icon(
          imageVector = if (isIngredientsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = "Toggle Steps",
          tint = TextMuted,
          modifier = Modifier.size(16.dp)
        )
      }

      AnimatedVisibility(visible = isIngredientsExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
        ) {
          recipe.preparationSteps.forEachIndexed { sIdx, step ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
              Text(text = "${sIdx + 1}. ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTealPrimary)
              Text(text = step, fontSize = 11.sp, color = DarkCharcoalText)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Buttons: Add to Plan vs Log Now (blocked if REJECTED per PRD Section 7.1)
      val isRejected = validation.overallStatus == ValidationStatus.REJECTED

      if (isRejected) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFEE2E2))
            .padding(10.dp)
        ) {
          Text(
            text = "Action Blocked: Recipe violates critical health/pharmacological restrictions.",
            color = Color(0xFF991B1B),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          OutlinedButton(
            onClick = onAddToPlan,
            modifier = Modifier
              .weight(1f)
              .testTag("add_to_plan_btn_${recipe.id}"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTealDark)
          ) {
            Text("Add to Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = onLogConsumed,
            modifier = Modifier
              .weight(1f)
              .testTag("log_consumed_btn_${recipe.id}"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
          ) {
            Text("Log as Consumed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
private fun NutrientMiniBox(label: String, value: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(Color(0xFFF4F6EA))
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Column {
      Text(text = label, fontSize = 10.sp, color = TextMuted)
      Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
  }
}
