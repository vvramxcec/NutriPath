package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.MealPlanEntity
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowAccent
import com.example.ui.theme.LimeYellowBackground
import java.util.Locale
import com.example.ui.theme.StatusApprovedBg
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.StatusModifiedBg
import com.example.ui.theme.StatusModifiedBlue
import com.example.ui.theme.StatusRejectedBg
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard

@Composable
fun MealCard(
  meal: MealPlanEntity,
  onLogConsumed: (Long) -> Unit,
  onMarkPlanned: (Long) -> Unit,
  onEditServings: (MealPlanEntity) -> Unit,
  onDeleteMeal: (Long) -> Unit,
  onModifyInPlanner: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var isExpanded by remember { mutableStateOf(false) }
  val isConsumed = meal.status == MealPlanEntity.STATUS_CONSUMED

  val (statusBg, statusText, statusBorder) = when (meal.validationStatus.uppercase()) {
    "APPROVED" -> Triple(StatusApprovedBg, StatusApprovedGreen, StatusApprovedGreen.copy(alpha = 0.3f))
    "MODIFIED" -> Triple(StatusModifiedBg, StatusModifiedBlue, StatusModifiedBlue.copy(alpha = 0.3f))
    "WARNING" -> Triple(StatusWarningBg, StatusWarningAmber, StatusWarningAmber.copy(alpha = 0.3f))
    else -> Triple(StatusRejectedBg, StatusRejectedRed, StatusRejectedRed.copy(alpha = 0.3f))
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("meal_card_${meal.mealType.lowercase()}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top row: Meal type & Consumed vs Planned status badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(LimeYellowBackground),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Restaurant,
              contentDescription = meal.mealType,
              tint = DeepTealDark,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = meal.mealType,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTealDark
          )
        }

        // Status badge: CONSUMED vs PLANNED
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isConsumed) StatusApprovedBg else Color(0xFFF1F3F0))
            .border(
              1.dp,
              if (isConsumed) StatusApprovedGreen.copy(alpha = 0.4f) else BorderSubtle,
              RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = if (isConsumed) Icons.Default.Check else Icons.Default.Schedule,
              contentDescription = if (isConsumed) "Logged" else "Planned",
              tint = if (isConsumed) StatusApprovedGreen else TextMuted,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isConsumed) "Logged as Consumed" else "Planned",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isConsumed) StatusApprovedGreen else TextMuted
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Meal title
      Text(
        text = meal.recipeTitle,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = DarkCharcoalText
      )

      if (meal.servings != 1.0f) {
        Text(
          text = "Servings: ${meal.servings}x portion",
          fontSize = 11.sp,
          color = TextMuted,
          fontWeight = FontWeight.Medium
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Nutrient pill badges: Calories, Protein, Carbs, Fat, Sodium
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        NutrientPill(label = "Cal", value = "${meal.caloriesKcal.toInt()} kcal", color = DeepTealPrimary)
        NutrientPill(label = "Pro", value = "${meal.proteinGrams.toInt()}g", color = Color(0xFF3730A3))
        NutrientPill(label = "Carb", value = "${meal.carbsGrams.toInt()}g", color = Color(0xFF854D0E))
        NutrientPill(label = "Fat", value = "${meal.fatGrams.toInt()}g", color = Color(0xFF9D174D))
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Clinical suitability summary
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(10.dp))
          .background(statusBg)
          .border(1.dp, statusBorder, RoundedCornerShape(10.dp))
          .padding(8.dp)
      ) {
        Text(
          text = if (meal.suitabilityExplanation.isNotBlank()) {
            "✓ ${meal.validationStatus.uppercase()}: ${meal.suitabilityExplanation}"
          } else {
            "✓ ${meal.validationStatus.uppercase()}: Verified safe for health conditions."
          },
          fontSize = 11.sp,
          color = statusText,
          lineHeight = 15.sp,
          fontWeight = FontWeight.Medium
        )
      }

      // Expandable section for ingredients and details
      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
        ) {
          if (meal.ingredientsSummary.isNotBlank()) {
            Text(
              text = "Ingredients & Quantities:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )
            Text(
              text = meal.ingredientsSummary,
              fontSize = 11.sp,
              color = TextMuted,
              lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
          }

          if (meal.instructionsSummary.isNotBlank()) {
            Text(
              text = "Preparation:",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )
            Text(
              text = meal.instructionsSummary,
              fontSize = 11.sp,
              color = TextMuted,
              lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
          }

          Text(
            text = "Nutrient Reference: ICMR-NIN IFCT 2017 & USDA FoodData Central (Deterministic).",
            fontSize = 9.sp,
            color = TextMuted
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Action Section Tier 1: Prominent Log Action Button
      if (!isConsumed) {
        Button(
          onClick = { onLogConsumed(meal.id) },
          colors = ButtonDefaults.buttonColors(
            containerColor = DeepTealPrimary,
            contentColor = Color.White
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("log_meal_button_${meal.id}")
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = LimeYellowAccent,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Log as Consumed (${meal.caloriesKcal.toInt()} kcal)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      } else {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StatusApprovedBg)
            .border(1.dp, StatusApprovedGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = StatusApprovedGreen,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Logged as Consumed",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = StatusApprovedGreen
            )
          }
          TextButton(
            onClick = { onMarkPlanned(meal.id) },
            modifier = Modifier.testTag("mark_planned_button_${meal.id}")
          ) {
            Text(
              text = "Undo",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = TextMuted
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Action Section Tier 2: Portion Adjuster & Utility Controls
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Interactive Portion Badge (Tapping opens Adjust Portion Dialog)
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F0))
            .clickable { onEditServings(meal) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("edit_servings_${meal.id}"),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Portion",
            tint = DeepTealPrimary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Portion: ${String.format(Locale.US, "%.1f", meal.servings)}x",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepTealDark
          )
        }

        // Secondary Utilities
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (onModifyInPlanner != null) {
            IconButton(
              onClick = onModifyInPlanner,
              modifier = Modifier
                .size(36.dp)
                .testTag("modify_in_planner_${meal.id}")
            ) {
              Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Modify in Planner",
                tint = DeepTealPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          IconButton(
            onClick = { isExpanded = !isExpanded },
            modifier = Modifier
              .size(36.dp)
              .testTag("expand_meal_${meal.id}")
          ) {
            Icon(
              imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
              contentDescription = "Toggle Recipe Details",
              tint = TextMuted,
              modifier = Modifier.size(20.dp)
            )
          }

          IconButton(
            onClick = { onDeleteMeal(meal.id) },
            modifier = Modifier
              .size(36.dp)
              .testTag("delete_meal_${meal.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete Meal",
              tint = StatusRejectedRed,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NutrientPill(label: String, value: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.1f))
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "$label: ",
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium,
        color = TextMuted
      )
      Text(
        text = value,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}
