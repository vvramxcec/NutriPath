package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.data.model.ValidationResult
import com.example.data.model.ValidationStatus
import com.example.ui.theme.StatusApprovedBg
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.StatusModifiedBg
import com.example.ui.theme.StatusModifiedBlue
import com.example.ui.theme.StatusRejectedBg
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.StatusWarningBg

@Composable
fun ValidationBadge(
  result: ValidationResult,
  modifier: Modifier = Modifier,
  expandable: Boolean = true
) {
  var isExpanded by remember { mutableStateOf(false) }

  val (bgColor, textColor, icon, label) = when (result.overallStatus) {
    ValidationStatus.APPROVED -> Quadruple(
      StatusApprovedBg,
      StatusApprovedGreen,
      Icons.Default.CheckCircle,
      "Approved for Profile"
    )
    ValidationStatus.MODIFIED -> Quadruple(
      StatusModifiedBg,
      StatusModifiedBlue,
      Icons.Default.Info,
      "Modified & Revalidated"
    )
    ValidationStatus.WARNING -> Quadruple(
      StatusWarningBg,
      StatusWarningAmber,
      Icons.Default.Warning,
      "Precautionary Notice"
    )
    ValidationStatus.REJECTED -> Quadruple(
      StatusRejectedBg,
      StatusRejectedRed,
      Icons.Default.Error,
      "Safety Check Rejected"
    )
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .border(1.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
      .clickable(enabled = expandable) { isExpanded = !isExpanded }
      .padding(horizontal = 12.dp, vertical = 8.dp)
      .testTag("validation_badge_${result.overallStatus.name.lowercase()}")
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = textColor,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = label,
          color = textColor,
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp
        )
      }

      if (expandable) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = if (isExpanded) "Hide details" else "View audit",
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
          )
          Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = "Toggle validation audit",
            tint = textColor,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // Suitability explanation text
    if (result.suitabilityExplanation.isNotBlank()) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = result.suitabilityExplanation,
        color = textColor.copy(alpha = 0.9f),
        fontSize = 12.sp,
        lineHeight = 16.sp
      )
    }

    // Expandable audit log
    AnimatedVisibility(visible = isExpanded) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(textColor.copy(alpha = 0.2f))
        )
        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = "Deterministic Safety Rule Checks (${result.rulesApplied.size} applied):",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = textColor
        )

        Spacer(modifier = Modifier.height(4.dp))
        result.checkItems.forEach { check ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 2.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(
                  when (check.status) {
                    ValidationStatus.APPROVED -> StatusApprovedGreen
                    ValidationStatus.MODIFIED -> StatusModifiedBlue
                    ValidationStatus.WARNING -> StatusWarningAmber
                    ValidationStatus.REJECTED -> StatusRejectedRed
                  }
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "${check.ruleName} [${check.ruleId}]",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
              )
              Text(
                text = check.message,
                fontSize = 10.sp,
                color = textColor.copy(alpha = 0.85f)
              )
            }
          }
        }

        if (result.appliedModifications.isNotEmpty()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Modifications Applied:",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
          )
          result.appliedModifications.forEach { mod ->
            Text(
              text = "• $mod",
              fontSize = 10.sp,
              color = textColor.copy(alpha = 0.85f),
              modifier = Modifier.padding(start = 4.dp, top = 1.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = result.clinicalDisclaimer,
          fontSize = 9.sp,
          color = textColor.copy(alpha = 0.65f),
          lineHeight = 12.sp,
          fontWeight = FontWeight.Normal
        )
      }
    }
  }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
