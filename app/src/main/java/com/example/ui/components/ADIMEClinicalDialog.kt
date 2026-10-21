package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ADIMEActionType
import com.example.data.model.ADIMEClinicalRecord
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard

/**
 * ADIME Clinical Inspector Dialog.
 * Implements NutriOrion (arXiv:2602.18650) standardized clinical review:
 * Assessment, Diagnosis, Intervention (Continue/Replace/Add), and Monitoring.
 */
@Composable
fun ADIMEClinicalDialog(
  adimeRecord: ADIMEClinicalRecord,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier
      .fillMaxWidth(0.96f)
      .testTag("adime_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.HealthAndSafety,
            contentDescription = null,
            tint = DeepTealPrimary,
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "NutriOrion ADIME Review",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
        }
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
          Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = DarkCharcoalText)
        }
      }
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
      ) {
        item {
          Text(
            text = adimeRecord.recipeTitle,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepTealDark
          )
          Text(
            text = "Nutrition Care Process (NCP) clinical evaluation and multi-objective SUM score mapping.",
            fontSize = 12.sp,
            color = TextMuted
          )
        }

        // Section A: Assessment
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9F0)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "A: Clinical Assessment",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTealPrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = adimeRecord.assessment.patientSummary,
                fontSize = 12.sp,
                color = DarkCharcoalText
              )
              Text(
                text = adimeRecord.assessment.biomarkersSummary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = DeepTealDark
              )
              if (adimeRecord.assessment.activeMedications.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Active Medications: ${adimeRecord.assessment.activeMedications.joinToString(", ")}",
                  fontSize = 11.sp,
                  color = DarkCharcoalText
                )
              }
            }
          }
        }

        // Section D: Nutrition Diagnoses
        item {
          Text(
            text = "D: Prioritized Diagnoses (SUM Multi-Objective)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
        }

        items(adimeRecord.diagnoses) { diag ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8D0)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = diag.description,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = DarkCharcoalText,
                  modifier = Modifier.weight(1f)
                )
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                      if (diag.severityLevel == "Critical") StatusRejectedRed.copy(alpha = 0.15f)
                      else StatusWarningAmber.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = "${diag.severityLevel} (SUM: ${diag.sumPriorityScore})",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (diag.severityLevel == "Critical") StatusRejectedRed else StatusWarningAmber
                  )
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Etiology: ${diag.etiology}",
                fontSize = 11.sp,
                color = TextMuted
              )
              Text(
                text = "Signs/Symptoms: ${diag.signsSymptoms}",
                fontSize = 11.sp,
                color = DarkCharcoalText
              )
            }
          }
        }

        // Section I: Interventions
        item {
          Text(
            text = "I: Targeted Interventions (${adimeRecord.interventions.size})",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
        }

        items(adimeRecord.interventions) { interv ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(WarmWhiteCard, RoundedCornerShape(8.dp))
              .border(1.dp, Color(0xFFE2E8D0), RoundedCornerShape(8.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.Top
          ) {
            val (icon, color, label) = when (interv.action) {
              ADIMEActionType.CONTINUE -> Triple(Icons.Default.CheckCircle, StatusApprovedGreen, "CONTINUE")
              ADIMEActionType.REPLACE -> Triple(Icons.Default.SwapHoriz, Color(0xFF0284C7), "REPLACE")
              ADIMEActionType.ADD -> Triple(Icons.Default.AddCircle, DeepTealPrimary, "ADD")
            }

            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = label,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = color
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = interv.foodItem,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = DarkCharcoalText
                )
              }
              if (interv.suggestedSubstitute != null) {
                Text(
                  text = "→ Swap: ${interv.suggestedSubstitute}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = DeepTealDark
                )
              }
              Text(
                text = interv.clinicalReason,
                fontSize = 11.sp,
                color = TextMuted
              )
            }
          }
        }

        // Section M: Monitoring
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9F0)),
            shape = RoundedCornerShape(10.dp)
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "M & E: Monitoring & Evaluation",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DeepTealPrimary
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Key Metrics: ${adimeRecord.monitoring.keyParametersToTrack.joinToString(" • ")}",
                fontSize = 11.sp,
                color = DarkCharcoalText
              )
              Text(
                text = "Schedule: ${adimeRecord.monitoring.evaluationSchedule}",
                fontSize = 11.sp,
                color = TextMuted
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
      }
    }
  )
}
