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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.MacroCarbsText
import com.example.ui.theme.MacroFatText
import com.example.ui.theme.MacroProteinText
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard
import com.example.ui.viewmodel.NutriPathViewModel

@Composable
fun MealHistoryScreen(
  viewModel: NutriPathViewModel,
  modifier: Modifier = Modifier
) {
  val historyMeals by viewModel.mealHistory.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(LimeYellowBackground)
      .testTag("meal_history_lazy_column"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(LimeYellowBackground),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.History, contentDescription = null, tint = DeepTealPrimary, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Logged Meal History", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
            Text("Archived records of verified consumed nutrition", fontSize = 12.sp, color = TextMuted)
          }
        }
      }
    }

    if (historyMeals.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmWhiteCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No logged meals in history yet.\nTap 'Log as Consumed' on planned meals to build your nutritional history.",
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      items(historyMeals, key = { it.id }) { meal ->
        HistoryMealCard(meal = meal)
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

@Composable
fun HistoryMealCard(meal: MealPlanEntity) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${meal.mealType} • ${meal.date}",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = DeepTealPrimary
        )
        Text(
          text = "✓ Logged",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = StatusApprovedGreen
        )
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = meal.recipeTitle,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = DarkCharcoalText
      )

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        HistoryPill("Cal", "${meal.caloriesKcal.toInt()} kcal", DeepTealPrimary)
        HistoryPill("Pro", "${meal.proteinGrams.toInt()}g", MacroProteinText)
        HistoryPill("Carb", "${meal.carbsGrams.toInt()}g", MacroCarbsText)
        HistoryPill("Fat", "${meal.fatGrams.toInt()}g", MacroFatText)
      }

      if (meal.ingredientsSummary.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Ingredients: ${meal.ingredientsSummary}",
          fontSize = 11.sp,
          color = TextMuted,
          lineHeight = 15.sp
        )
      }
    }
  }
}

@Composable
private fun HistoryPill(label: String, value: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(color.copy(alpha = 0.08f))
      .padding(horizontal = 6.dp, vertical = 3.dp)
  ) {
    Text(
      text = "$label: $value",
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      color = color
    )
  }
}
