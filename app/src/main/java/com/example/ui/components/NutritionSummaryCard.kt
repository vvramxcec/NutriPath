package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.data.model.HealthProfile
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealContainer
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowAccent
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.MacroCarbsText
import com.example.ui.theme.MacroFatText
import com.example.ui.theme.MacroProteinText
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard

@Composable
fun NutritionSummaryCard(
  profile: HealthProfile,
  consumedCalories: Float,
  consumedProtein: Float,
  consumedCarbs: Float,
  consumedFat: Float,
  consumedFiber: Float,
  consumedSodium: Float,
  selectedDateLabel: String,
  modifier: Modifier = Modifier
) {
  val targetCalories = profile.dailyCalorieTarget
  val remainingCalories = (targetCalories - consumedCalories).coerceAtLeast(0f)
  val calFraction = if (targetCalories > 0) (consumedCalories / targetCalories).coerceIn(0f, 1f) else 0f
  val animatedCalFraction by animateFloatAsState(targetValue = calFraction, label = "cal_progress")

  val hasHypertension = profile.medicalConditions.any { it.contains("Hypertension", ignoreCase = true) }
  val hasDiabetes = profile.medicalConditions.any { it.contains("Diabetes", ignoreCase = true) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("nutrition_summary_card"),
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp)
    ) {
      // Header with date & health goal indicator
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Daily Nutrition Summary",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
          Text(
            text = selectedDateLabel,
            fontSize = 12.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(LimeYellowBackground)
            .border(1.dp, LimeYellowAccent, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.HealthAndSafety,
              contentDescription = "Condition Active",
              tint = DeepTealPrimary,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (profile.medicalConditions.isNotEmpty()) profile.medicalConditions.first() else "Personalized",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = DeepTealDark
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Primary row: Circular gauge for calories + consumed / target / remaining numbers
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Calorie Gauge
        Box(
          contentAlignment = Alignment.Center,
          modifier = Modifier.size(96.dp)
        ) {
          CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(96.dp),
            color = LimeYellowBackground,
            strokeWidth = 9.dp
          )
          CircularProgressIndicator(
            progress = { animatedCalFraction },
            modifier = Modifier.size(96.dp),
            color = DeepTealPrimary,
            strokeWidth = 9.dp
          )
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.ElectricBolt,
              contentDescription = "Calories",
              tint = DeepTealPrimary,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = "${consumedCalories.toInt()}",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )
            Text(
              text = "kcal",
              fontSize = 10.sp,
              color = TextMuted
            )
          }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Breakdown stats: Consumed vs Target & Remaining
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Target:", fontSize = 13.sp, color = TextMuted)
            Text(
              text = "$targetCalories kcal",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = DarkCharcoalText
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Consumed:", fontSize = 13.sp, color = TextMuted)
            Text(
              text = "${consumedCalories.toInt()} kcal",
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = DeepTealPrimary
            )
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(DeepTealContainer.copy(alpha = 0.5f))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Remaining:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = DeepTealDark
              )
              Text(
                text = "${remainingCalories.toInt()} kcal",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTealDark
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // Section for all 3 macronutrients: Protein, Carbs, Fat (PRD mandatory)
      Text(
        text = "Macronutrient Distribution",
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkCharcoalText
      )
      Spacer(modifier = Modifier.height(8.dp))

      MacroProgressBar(
        label = "Protein",
        currentGrams = consumedProtein,
        targetGrams = profile.proteinTargetG.toFloat(),
        barColor = MacroProteinText,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      MacroProgressBar(
        label = "Carbohydrates",
        currentGrams = consumedCarbs,
        targetGrams = profile.carbsTargetG.toFloat(),
        barColor = MacroCarbsText,
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(8.dp))

      MacroProgressBar(
        label = "Healthy Fats",
        currentGrams = consumedFat,
        targetGrams = profile.fatTargetG.toFloat(),
        barColor = MacroFatText,
        modifier = Modifier.fillMaxWidth()
      )

      // Condition-specific clinical monitors
      if (hasHypertension || hasDiabetes) {
        Spacer(modifier = Modifier.height(14.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF7F8F0))
            .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp))
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          if (hasHypertension) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "Sodium (HTN Cap)", fontSize = 10.sp, color = TextMuted)
              Text(
                text = "${consumedSodium.toInt()} / ${profile.maxSodiumMg} mg",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (consumedSodium > profile.maxSodiumMg) StatusWarningAmber else DeepTealDark
              )
            }
          }
          if (hasDiabetes) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "Fiber (Glycemic Buffer)", fontSize = 10.sp, color = TextMuted)
              Text(
                text = "${consumedFiber.toInt()}g consumed",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTealDark
              )
            }
          }
        }
      }
    }
  }
}
