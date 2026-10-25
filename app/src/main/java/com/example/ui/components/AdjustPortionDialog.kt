package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.theme.MacroCarbsText
import com.example.ui.theme.MacroFatText
import com.example.ui.theme.MacroProteinText
import com.example.ui.theme.TextMuted
import java.util.Locale

@Composable
fun AdjustPortionDialog(
  meal: MealPlanEntity,
  onDismiss: () -> Unit,
  onConfirm: (Float) -> Unit,
  modifier: Modifier = Modifier
) {
  var portionMultiplier by remember(meal) {
    mutableFloatStateOf(if (meal.servings > 0f) meal.servings else 1.0f)
  }

  val baseServings = if (meal.servings > 0f) meal.servings else 1.0f
  val ratio = portionMultiplier / baseServings
  val adjustedCalories = (meal.caloriesKcal * ratio).toInt()
  val adjustedProtein = (meal.proteinGrams * ratio).toInt()
  val adjustedCarbs = (meal.carbsGrams * ratio).toInt()
  val adjustedFat = (meal.fatGrams * ratio).toInt()

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp)
      .testTag("adjust_portion_dialog"),
    containerColor = Color.White,
    shape = RoundedCornerShape(24.dp),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(LimeYellowBackground),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Tune,
              contentDescription = null,
              tint = DeepTealPrimary,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Adjust Portion Size",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
        }
        IconButton(
          onClick = onDismiss,
          modifier = Modifier.size(32.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth()
      ) {
        // Meal Title Badge
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF7F9F6))
            .border(1.dp, BorderSubtle, RoundedCornerShape(12.dp))
            .padding(12.dp)
        ) {
          Column {
            Text(
              text = meal.recipeTitle,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = DeepTealDark
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "${meal.mealType} • Standard: ${meal.caloriesKcal.toInt()} kcal",
              fontSize = 12.sp,
              color = TextMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Calculation Card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LimeYellowBackground)
            .border(1.dp, LimeYellowAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            .padding(14.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Selected Portion",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
              )
              Text(
                text = "${String.format(Locale.US, "%.1f", portionMultiplier)}x Servings",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DeepTealDark
              )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = DeepTealPrimary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "$adjustedCalories kcal",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoalText
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Macro breakdown row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          PortionMacroStat("Protein", "${adjustedProtein}g", MacroProteinText)
          PortionMacroStat("Carbs", "${adjustedCarbs}g", MacroCarbsText)
          PortionMacroStat("Fats", "${adjustedFat}g", MacroFatText)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Preset Chips
        Text(
          text = "Quick Presets",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextMuted
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val presets = listOf(
            0.5f to "0.5x Half",
            1.0f to "1.0x Normal",
            1.5f to "1.5x Large",
            2.0f to "2.0x Double"
          )
          presets.forEach { (factor, label) ->
            val isSelected = Math.abs(portionMultiplier - factor) < 0.05f
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) DeepTealPrimary else Color(0xFFF1F4F0))
                .border(
                  width = 1.dp,
                  color = if (isSelected) DeepTealPrimary else BorderSubtle,
                  shape = RoundedCornerShape(10.dp)
                )
                .clickable { portionMultiplier = factor }
                .padding(vertical = 8.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else DarkCharcoalText
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Fine Tuning Slider
        Text(
          text = "Fine Tune Multiplier",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextMuted
        )
        Slider(
          value = portionMultiplier,
          onValueChange = {
            portionMultiplier = (Math.round(it * 10f) / 10f).coerceIn(0.5f, 3.0f)
          },
          valueRange = 0.5f..3.0f,
          steps = 24,
          colors = SliderDefaults.colors(
            thumbColor = DeepTealPrimary,
            activeTrackColor = DeepTealPrimary,
            inactiveTrackColor = Color(0xFFE2E8F0)
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("portion_slider")
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text("0.5x (Light)", fontSize = 11.sp, color = TextMuted)
          Text("1.0x (Regular)", fontSize = 11.sp, color = TextMuted)
          Text("3.0x (Max)", fontSize = 11.sp, color = TextMuted)
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(portionMultiplier) },
        colors = ButtonDefaults.buttonColors(
          containerColor = DeepTealPrimary,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .height(46.dp)
          .testTag("save_portion_button")
      ) {
        Text(
          text = "Save Portion",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
      }
    },
    dismissButton = {
      TextButton(
        onClick = onDismiss,
        modifier = Modifier.height(46.dp)
      ) {
        Text(
          text = "Cancel",
          color = TextMuted,
          fontSize = 14.sp
        )
      }
    }
  )
}

@Composable
private fun PortionMacroStat(
  name: String,
  value: String,
  color: Color
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFFF7F9F6))
      .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
      .padding(horizontal = 14.dp, vertical = 6.dp)
  ) {
    Text(text = name, fontSize = 10.sp, color = TextMuted)
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = color
    )
  }
}
