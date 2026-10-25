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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MoreVert
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

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.AdjustPortionDialog
import java.util.Locale

@Composable
fun HomeScreen(
  viewModel: NutriPathViewModel,
  onNavigateToGenerator: (String?) -> Unit,
  onNavigateToPlanner: () -> Unit = {},
  onOpenMenu: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val profile by viewModel.healthProfile.collectAsStateWithLifecycle()
  val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
  val meals by viewModel.currentMeals.collectAsStateWithLifecycle()
  val selectedDateDisplay by viewModel.selectedDateDisplay.collectAsStateWithLifecycle()
  val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

  val greetingName = remember(profile.name, currentUser) {
    val raw = profile.name.trim().ifBlank {
      currentUser?.displayName?.trim()?.ifBlank { null }
        ?: currentUser?.email?.substringBefore("@")?.replace(".", " ")?.replace("_", " ")
    } ?: ""
    if (raw.isNotBlank()) {
      val firstWord = raw.split(" ").firstOrNull { it.isNotBlank() } ?: raw
      firstWord.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    } else {
      ""
    }
  }

  val avatarInitials = remember(greetingName) {
    if (greetingName.isNotBlank()) {
      greetingName.take(2).uppercase(Locale.getDefault())
    } else {
      "NP"
    }
  }

  val consumedCal by viewModel.consumedCalories.collectAsStateWithLifecycle()
  val consumedPro by viewModel.consumedProtein.collectAsStateWithLifecycle()
  val consumedCarb by viewModel.consumedCarbs.collectAsStateWithLifecycle()
  val consumedFat by viewModel.consumedFat.collectAsStateWithLifecycle()
  val consumedFib by viewModel.consumedFiber.collectAsStateWithLifecycle()
  val consumedSod by viewModel.consumedSodium.collectAsStateWithLifecycle()

  val context = LocalContext.current
  var mealToEditServings by remember { mutableStateOf<MealPlanEntity?>(null) }

  // High-contrast Adjust Portion Dialog with live recalculation
  if (mealToEditServings != null) {
    val meal = mealToEditServings!!
    AdjustPortionDialog(
      meal = meal,
      onDismiss = { mealToEditServings = null },
      onConfirm = { newServings ->
        viewModel.updateMealServings(meal, newServings)
        mealToEditServings = null
        Toast.makeText(
          context,
          "Portion updated to ${String.format(Locale.US, "%.1f", newServings)}x for '${meal.recipeTitle}'",
          Toast.LENGTH_SHORT
        ).show()
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
    // 0. Top Header Row with Logo, Title, and 3-Dot Menu Button (No separate top bar needed!)
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(DeepTealPrimary),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = avatarInitials,
              color = LimeYellowAccent,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = if (greetingName.isNotBlank()) "Hai, $greetingName 👋" else "NutriPath",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = DeepTealDark
            )
            Text(
              text = if (greetingName.isNotBlank()) "Your Personalized Nutrition" else "Clinical Nutrition & Diet",
              fontSize = 11.sp,
              color = TextMuted
            )
          }
        }

        IconButton(
          onClick = onOpenMenu,
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(WarmWhiteCard)
            .border(1.dp, BorderSubtle, CircleShape)
            .testTag("home_3_dot_menu_button")
        ) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Menu & Settings",
            tint = DeepTealDark
          )
        }
      }
    }

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

    // 2. Nutrition Summary Card with Calorie & Macro Trackers
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

    // 3. Prominent "Generate Full Day Plan" Action Button (Tailored to conditions, saved as PLANNED, not auto-logged)
    item {
      Column(modifier = Modifier.fillMaxWidth()) {
        Button(
          onClick = {
            viewModel.generateAndApplyFullDayPlanForSelectedDate()
            Toast.makeText(context, "Generating personalized clinical plan for $selectedDateDisplay...", Toast.LENGTH_SHORT).show()
          },
          enabled = !isGenerating,
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
              text = if (isGenerating) "Generating Personalized Plan..." else "Generate Full Day Plan",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "⚡ Built for your medical conditions & targets",
            fontSize = 11.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
          )
          TextButton(
            onClick = onNavigateToPlanner,
            modifier = Modifier.testTag("home_modify_in_planner_link")
          ) {
            Text(
              text = "Modify in Planner →",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = DeepTealPrimary
            )
          }
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
        Column {
          Text(
            text = "Today's Meal Schedule",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
          Text(
            text = "${meals.size} Planned / Logged • Only logged when confirmed",
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = TextMuted
          )
        }
        TextButton(
          onClick = onNavigateToPlanner,
          modifier = Modifier.testTag("header_planner_shortcut_btn")
        ) {
          Text(
            text = "Open Planner",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTealPrimary
          )
        }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = { viewModel.generateAndApplyFullDayPlanForSelectedDate() },
                colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary)
              ) {
                Text("Generate Full Day")
              }
              OutlinedButton(
                onClick = onNavigateToPlanner,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTealPrimary)
              ) {
                Text("Open Planner")
              }
            }
          }
        }
      }
    } else {
      items(meals, key = { it.id }) { mealItem ->
        MealCard(
          meal = mealItem,
          onLogConsumed = { id ->
            viewModel.markMealConsumed(id)
            Toast.makeText(context, "Logged '${mealItem.recipeTitle}' as Consumed! (+${mealItem.caloriesKcal.toInt()} kcal)", Toast.LENGTH_SHORT).show()
          },
          onMarkPlanned = { id ->
            viewModel.markMealPlanned(id)
            Toast.makeText(context, "Marked '${mealItem.recipeTitle}' as Planned.", Toast.LENGTH_SHORT).show()
          },
          onEditServings = { meal -> mealToEditServings = meal },
          onDeleteMeal = { id ->
            viewModel.deleteMeal(id)
            Toast.makeText(context, "Removed '${mealItem.recipeTitle}' from schedule.", Toast.LENGTH_SHORT).show()
          },
          onModifyInPlanner = onNavigateToPlanner
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
