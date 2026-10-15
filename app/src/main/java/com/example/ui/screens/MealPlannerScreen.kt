package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.model.MealPlanEntity
import com.example.ui.components.MealCard
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowAccent
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard
import com.example.ui.viewmodel.NutriPathViewModel

@Composable
fun MealPlannerScreen(
  viewModel: NutriPathViewModel,
  onNavigateToGenerator: (String?) -> Unit,
  modifier: Modifier = Modifier
) {
  val profile by viewModel.healthProfile.collectAsStateWithLifecycle()
  val meals by viewModel.currentMeals.collectAsStateWithLifecycle()
  val selectedDateDisplay by viewModel.selectedDateDisplay.collectAsStateWithLifecycle()

  var mealToEditServings by remember { mutableStateOf<MealPlanEntity?>(null) }

  // Portion edit dialog
  if (mealToEditServings != null) {
    val meal = mealToEditServings!!
    var portionFactor by remember(meal) { mutableFloatStateOf(meal.servings) }

    AlertDialog(
      onDismissRequest = { mealToEditServings = null },
      title = { Text(text = "Edit Meal Servings", fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text(text = meal.recipeTitle, fontSize = 14.sp, color = DeepTealPrimary, fontWeight = FontWeight.SemiBold)
          Spacer(modifier = Modifier.height(10.dp))
          Text(text = "Portion size: ${String.format("%.1f", portionFactor)}x (${(meal.caloriesKcal / meal.servings * portionFactor).toInt()} kcal)")
          Slider(
            value = portionFactor,
            onValueChange = { portionFactor = (Math.round(it * 2) / 2f).coerceIn(0.5f, 3.0f) },
            valueRange = 0.5f..3.0f,
            steps = 4
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateMealServings(meal, portionFactor)
            mealToEditServings = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { mealToEditServings = null }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }

  val totalPlannedCalories = meals.sumOf { it.caloriesKcal.toDouble() }.toFloat()
  val totalPlannedProtein = meals.sumOf { it.proteinGrams.toDouble() }.toFloat()
  val totalPlannedCarbs = meals.sumOf { it.carbsGrams.toDouble() }.toFloat()
  val totalPlannedFat = meals.sumOf { it.fatGrams.toDouble() }.toFloat()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(LimeYellowBackground)
      .testTag("meal_planner_lazy_column"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // 1. Date Header & Controls
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(WarmWhiteCard)
          .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = { viewModel.changeSelectedDate(-1) },
          modifier = Modifier.testTag("planner_prev_date_btn")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev Day", tint = DeepTealDark)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CalendarToday, contentDescription = null, tint = DeepTealPrimary, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = selectedDateDisplay,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
        }

        IconButton(
          onClick = { viewModel.changeSelectedDate(1) },
          modifier = Modifier.testTag("planner_next_date_btn")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day", tint = DeepTealDark)
        }
      }
    }

    // 2. Day Plan Budget Overview
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Day Planning & Nutrition Balance",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Total scheduled: ${totalPlannedCalories.toInt()} / ${profile.dailyCalorieTarget} kcal (${if (profile.dailyCalorieTarget > 0) (totalPlannedCalories / profile.dailyCalorieTarget * 100).toInt() else 0}%)",
            fontSize = 13.sp,
            color = DeepTealDark,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            PlannerStatChip("Protein", "${totalPlannedProtein.toInt()}/${profile.proteinTargetG}g")
            PlannerStatChip("Carbs", "${totalPlannedCarbs.toInt()}/${profile.carbsTargetG}g")
            PlannerStatChip("Fat", "${totalPlannedFat.toInt()}/${profile.fatTargetG}g")
          }
        }
      }
    }

    // 3. Quick Actions: Generate Full Day or Individual Meal
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { onNavigateToGenerator("Full-Day Plan") },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("planner_gen_full_day_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LimeYellowAccent, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Generate Full Day", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = { onNavigateToGenerator(null) },
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("planner_add_meal_btn"),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTealDark)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Single Meal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }

    // 4. Section Title
    item {
      Text(
        text = "Scheduled Meals (${meals.size})",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DarkCharcoalText
      )
    }

    // 5. Meals for Date
    if (meals.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmWhiteCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No meals scheduled for $selectedDateDisplay.\nTap 'Generate Full Day' to build an instant clinical plan.",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      items(meals, key = { it.id }) { mealItem ->
        MealCard(
          meal = mealItem,
          onLogConsumed = { id -> viewModel.markMealConsumed(id) },
          onMarkPlanned = { id -> viewModel.markMealPlanned(id) },
          onEditServings = { meal -> mealToEditServings = meal },
          onDeleteMeal = { id -> viewModel.deleteMeal(id) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

@Composable
private fun PlannerStatChip(label: String, value: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFFEFF5EB))
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Column {
      Text(text = label, fontSize = 10.sp, color = TextMuted)
      Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepTealDark)
    }
  }
}
