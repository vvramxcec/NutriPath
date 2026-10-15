package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.components.NutritionSummaryCard
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
fun HomeScreen(
  viewModel: NutriPathViewModel,
  onNavigateToGenerator: (String?) -> Unit,
  modifier: Modifier = Modifier
) {
  val profile by viewModel.healthProfile.collectAsStateWithLifecycle()
  val meals by viewModel.currentMeals.collectAsStateWithLifecycle()
  val selectedDateDisplay by viewModel.selectedDateDisplay.collectAsStateWithLifecycle()

  val consumedCal by viewModel.consumedCalories.collectAsStateWithLifecycle()
  val consumedPro by viewModel.consumedProtein.collectAsStateWithLifecycle()
  val consumedCarb by viewModel.consumedCarbs.collectAsStateWithLifecycle()
  val consumedFat by viewModel.consumedFat.collectAsStateWithLifecycle()
  val consumedFib by viewModel.consumedFiber.collectAsStateWithLifecycle()
  val consumedSod by viewModel.consumedSodium.collectAsStateWithLifecycle()

  var mealToEditServings by remember { mutableStateOf<MealPlanEntity?>(null) }

  // Portion edit dialog
  if (mealToEditServings != null) {
    val meal = mealToEditServings!!
    var portionFactor by remember(meal) { mutableFloatStateOf(meal.servings) }

    AlertDialog(
      onDismissRequest = { mealToEditServings = null },
      title = {
        Text(
          text = "Adjust Portion Size",
          fontWeight = FontWeight.Bold,
          color = DarkCharcoalText
        )
      },
      text = {
        Column {
          Text(
            text = meal.recipeTitle,
            fontSize = 14.sp,
            color = DeepTealPrimary,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Portion multiplier: ${String.format("%.1f", portionFactor)}x (${(meal.caloriesKcal / meal.servings * portionFactor).toInt()} kcal)",
            fontSize = 13.sp,
            color = DarkCharcoalText
          )
          Slider(
            value = portionFactor,
            onValueChange = { portionFactor = (Math.round(it * 2) / 2f).coerceIn(0.5f, 3.0f) },
            valueRange = 0.5f..3.0f,
            steps = 4
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("0.5x (Half)", fontSize = 11.sp, color = TextMuted)
            Text("1.0x (Regular)", fontSize = 11.sp, color = TextMuted)
            Text("2.0x (Double)", fontSize = 11.sp, color = TextMuted)
          }
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
          Text("Update Nutrients")
        }
      },
      dismissButton = {
        TextButton(onClick = { mealToEditServings = null }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(LimeYellowBackground)
      .testTag("home_screen_lazy_column"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Date Navigator Bar
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
          modifier = Modifier.testTag("prev_date_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Previous Day",
            tint = DeepTealDark
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarToday,
            contentDescription = null,
            tint = DeepTealPrimary,
            modifier = Modifier.size(16.dp)
          )
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
          modifier = Modifier.testTag("next_date_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Next Day",
            tint = DeepTealDark
          )
        }
      }
    }

    // 2. PRD 4.3 Upper Card — Daily Nutrition Summary
    item {
      NutritionSummaryCard(
        profile = profile,
        consumedCalories = consumedCal,
        consumedProtein = consumedPro,
        consumedCarbs = consumedCarb,
        consumedFat = consumedFat,
        consumedFiber = consumedFib,
        consumedSodium = consumedSod,
        selectedDateLabel = selectedDateDisplay
      )
    }

    // 3. Prominent "Generate a Meal" Action Button
    item {
      Button(
        onClick = { onNavigateToGenerator(null) },
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("generate_meal_action_button"),
        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = LimeYellowAccent,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Generate a Personalized Meal",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }

    // 4. Section Header: Meals
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Today's Meal Schedule",
          fontSize = 17.sp,
          fontWeight = FontWeight.Bold,
          color = DarkCharcoalText
        )
        Text(
          text = "${meals.size} Planned / Logged",
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = TextMuted
        )
      }
    }

    // 5. Meals List (Breakfast, Lunch, Dinner, Snack)
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
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Restaurant,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No meals planned for this day yet.",
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = DarkCharcoalText
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
              onClick = { onNavigateToGenerator(null) },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTealPrimary)
            ) {
              Text("Generate Meal Now")
            }
          }
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
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
