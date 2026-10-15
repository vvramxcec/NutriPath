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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.engine.MealSafetyValidator
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard
import com.example.ui.viewmodel.NutriPathViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsSafetyScreen(
  viewModel: NutriPathViewModel,
  modifier: Modifier = Modifier
) {
  var showResetConfirmation by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  if (showResetConfirmation) {
    AlertDialog(
      onDismissRequest = { showResetConfirmation = false },
      title = { Text("Delete All User Data?") },
      text = {
        Text("This will purge your health profile, meal plans, and historical logs from local storage. This action is irreversible.")
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.clearAllData()
            showResetConfirmation = false
            scope.launch { snackbarHostState.showSnackbar("All user data purged successfully.") }
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRejectedRed)
        ) {
          Text("Confirm Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirmation = false }) {
          Text("Cancel", color = TextMuted)
        }
      }
    )
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(LimeYellowBackground)
        .testTag("settings_safety_lazy_column"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Header
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
              Icon(Icons.Default.Security, contentDescription = null, tint = DeepTealPrimary, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Safety & Clinical Engine", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
              Text("Deterministic rules & medical safety architecture", fontSize = 12.sp, color = TextMuted)
            }
          }
        }
      }

      // 2. Clinical Disclaimer
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Info, contentDescription = null, tint = DeepTealPrimary, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Clinical Safety Principles", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DeepTealDark)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "NutriPath enforces a strict separation between AI meal proposals and deterministic safety checks. AI models never certify clinical safety independently. All recommendations pass through reviewed clinical dietary rules and deterministic ICMR-NIN / USDA nutrient equations.",
              fontSize = 12.sp,
              color = TextMuted,
              lineHeight = 17.sp
            )
          }
        }
      }

      // 3. Reviewed Rules Catalog
      item {
        Text("Active Deterministic Safety Rules", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
      }

      items(MealSafetyValidator.REVIEWED_RULES) { (ruleId, ruleName) ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle))
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = DeepTealPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(text = ruleName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
              Text(text = "Rule ID: $ruleId • Status: Active & Reviewed", fontSize = 11.sp, color = TextMuted)
            }
          }
        }
      }

      // 4. Data Privacy & Account Management (PRD 11)
      item {
        Text("Privacy & Storage Management", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
      }

      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Data Storage", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "All health profiles and meal plans are stored securely in local on-device SQLite / Room database. No health data is sold or used for public AI training.",
              fontSize = 12.sp,
              color = TextMuted,
              lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
              onClick = { showResetConfirmation = true },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRejectedRed),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("purge_data_btn")
            ) {
              Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Purge All User Data & Reset", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(30.dp))
      }
    }

    SnackbarHost(
      hostState = snackbarHostState,
      modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
    )
  }
}
