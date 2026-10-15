package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.TextMuted

@Composable
fun MacroProgressBar(
  label: String,
  currentGrams: Float,
  targetGrams: Float,
  barColor: Color,
  modifier: Modifier = Modifier
) {
  val fraction = if (targetGrams > 0) (currentGrams / targetGrams).coerceIn(0f, 1f) else 0f
  val animatedProgress by animateFloatAsState(targetValue = fraction, label = "macro_progress")

  Column(modifier = modifier) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = DarkCharcoalText
      )
      Text(
        text = "${currentGrams.toInt()} / ${targetGrams.toInt()}g",
        fontSize = 11.sp,
        color = TextMuted,
        fontWeight = FontWeight.Medium
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(8.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(barColor.copy(alpha = 0.2f))
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(animatedProgress)
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(barColor)
      )
    }
  }
}
