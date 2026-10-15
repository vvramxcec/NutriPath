package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
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
import com.example.data.model.ValidationStatus
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

  val mealTypeOptions = listOf("Breakfast", "Lunch", "Dinner", "Snack", "Full-Day Plan")

  LaunchedEffect(initialMealType) {
    if (initialMealType != null) {
      viewModel.setGenerationMealType(initialMealType)
    } else if (candidates.isEmpty() && fullDayPlan == null) {
      viewModel.generateRecommendations(selectedType)
    }
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
                text = "Time-Aware Recommendation",
                fontSize = 12.sp,
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
                      .clip(RoundedCornerShape(3.dp))
                      .background(if (isSelected) LimeYellowAccent else DeepTealPrimary)
                  )
                }
              }
            }
          }
        }
      }

      // 3. User Health Profile Summary Pill
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFE5EED0))
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Evaluating for Health Profile:",
              fontSize = 11.sp,
              color = DeepTealDark,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "${profile.medicalConditions.joinToString(", ")} | Allergies: ${if (profile.allergies.isNotEmpty()) profile.allergies.joinToString(", ") else "None"}",
              fontSize = 11.sp,
              color = DarkCharcoalText
            )
          }

          IconButton(
            onClick = { viewModel.generateRecommendations() },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Regenerate",
              tint = DeepTealDark
            )
          }
        }
      }

      // 4. Loading or Empty state
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
                text = "Executing Nutrition Calculator & Safety Validator...",
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
        items(
          listOf(
            plan.breakfast to "Breakfast",
            plan.lunch to "Lunch",
            plan.dinner to "Dinner",
            plan.snack to "Snack"
          )
        ) { (candidate, type) ->
          CandidateRecipeCard(
            candidate = candidate,
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

      // 6. Individual Meal Candidates List
      if (candidates.isNotEmpty() && !isGenerating && fullDayPlan == null) {
        items(candidates, key = { it.recipe.id }) { candidate ->
          CandidateRecipeCard(
            candidate = candidate,
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

@Composable
fun CandidateRecipeCard(
  candidate: MealRecommendationEngine.RecommendedMealCandidate,
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
      // Header: Cuisine style & Time badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = recipe.cuisineStyle,
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = DeepTealPrimary
        )

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

      Spacer(modifier = Modifier.height(6.dp))

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

      Spacer(modifier = Modifier.height(12.dp))

      // Independent Meal Safety Validator Badge with audit
      ValidationBadge(
        result = validation,
        expandable = true,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Expandable Ingredients & Exact Gram Calculations
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0xFFF9FAF6))
          .clickable { isIngredientsExpanded = !isIngredientsExpanded }
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Calculated Ingredients (${recipe.ingredients.size})",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = DarkCharcoalText
        )
        Icon(
          imageVector = if (isIngredientsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
          contentDescription = "Toggle Ingredients",
          tint = TextMuted,
          modifier = Modifier.size(18.dp)
        )
      }

      AnimatedVisibility(visible = isIngredientsExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
        ) {
          recipe.ingredients.forEach { item ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "• ${item.displayName}",
                fontSize = 12.sp,
                color = DarkCharcoalText
              )
              Text(
                text = "${item.quantityGrams.toInt()}g",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTealDark
              )
            }
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Deterministic Formula: (quantity in grams / 100) × IFCT / USDA nutrient reference.",
            fontSize = 9.sp,
            color = TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

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
            text = "Action Blocked: Recipe violates critical health restrictions and cannot be recommended.",
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
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Log Consumed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.08f))
      .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 6.dp)
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = label, fontSize = 9.sp, color = TextMuted)
      Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
  }
}
