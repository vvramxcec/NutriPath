package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HealthProfile
import com.example.data.model.MedicalConditionCatalog
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowAccent
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard
import com.example.ui.viewmodel.NutriPathViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthProfileScreen(
  viewModel: NutriPathViewModel,
  modifier: Modifier = Modifier
) {
  val originalProfile by viewModel.healthProfile.collectAsStateWithLifecycle()

  var name by remember(originalProfile) { mutableStateOf(originalProfile.name) }
  var ageText by remember(originalProfile) { mutableStateOf(originalProfile.age.toString()) }
  var weightText by remember(originalProfile) { mutableStateOf(originalProfile.weightKg.toString()) }
  var heightText by remember(originalProfile) { mutableStateOf(originalProfile.heightCm.toString()) }
  var gender by remember(originalProfile) { mutableStateOf(originalProfile.gender) }
  var activityLevel by remember(originalProfile) { mutableStateOf(originalProfile.activityLevel) }

  var selectedConditions by remember(originalProfile) { mutableStateOf(originalProfile.medicalConditions.toSet()) }
  var selectedAllergies by remember(originalProfile) { mutableStateOf(originalProfile.allergies.toSet()) }
  var dietaryPreference by remember(originalProfile) { mutableStateOf(originalProfile.dietaryPreference) }

  var dailyCaloriesText by remember(originalProfile) { mutableStateOf(originalProfile.dailyCalorieTarget.toString()) }
  var maxSodiumText by remember(originalProfile) { mutableStateOf(originalProfile.maxSodiumMg.toString()) }
  var clinicianNotes by remember(originalProfile) { mutableStateOf(originalProfile.clinicianNotes) }
  var consentGiven by remember(originalProfile) { mutableStateOf(originalProfile.consentGiven) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(LimeYellowBackground)
        .testTag("health_profile_lazy_column"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Header Card
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
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LimeYellowBackground),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = DeepTealPrimary, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Personal Health Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
              Text("Underpins all meal safety rules and nutritional targets", fontSize = 12.sp, color = TextMuted)
            }
          }
        }
      }

      // 2. Body Metrics
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Body Measurements & Demographics", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)

            OutlinedTextField(
              value = name,
              onValueChange = { name = it },
              label = { Text("Full Name / Label") },
              modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepTealPrimary)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it },
                label = { Text("Age (yrs)") },
                modifier = Modifier.weight(1f).testTag("profile_age_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepTealPrimary)
              )
              OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.weight(1f).testTag("profile_weight_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepTealPrimary)
              )
              OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.weight(1f).testTag("profile_height_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepTealPrimary)
              )
            }

            Text("Activity Level:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarkCharcoalText)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              MedicalConditionCatalog.ACTIVITY_LEVELS.forEach { level ->
                FilterChip(
                  selected = activityLevel == level,
                  onClick = { activityLevel = level },
                  label = { Text(level, fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeepTealPrimary,
                    selectedLabelColor = Color.White
                  )
                )
              }
            }
          }
        }
      }

      // 3. Medical Conditions Controlled Catalogue
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Medical Conditions (Controlled List)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Select all diagnosed conditions. Each activates reviewed clinical safety rules.", fontSize = 11.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              MedicalConditionCatalog.AVAILABLE_CONDITIONS.forEach { condition ->
                val isSelected = selectedConditions.contains(condition)
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    selectedConditions = if (isSelected) selectedConditions - condition else selectedConditions + condition
                  },
                  label = { Text(condition, fontSize = 12.sp) },
                  leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) } } else null,
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeepTealPrimary,
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("chip_condition_${condition.lowercase().take(6)}")
                )
              }
            }
          }
        }
      }

      // 4. Food Allergies & Intolerances
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Food Allergies & Intolerances", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Ingredients matching these allergies will be strictly BLOCKED.", fontSize = 11.sp, color = StatusWarningAmber)
            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              MedicalConditionCatalog.AVAILABLE_ALLERGIES.forEach { allergy ->
                val isSelected = selectedAllergies.contains(allergy)
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    selectedAllergies = if (isSelected) selectedAllergies - allergy else selectedAllergies + allergy
                  },
                  label = { Text(allergy, fontSize = 12.sp) },
                  leadingIcon = if (isSelected) { { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) } } else null,
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFDC2626),
                    selectedLabelColor = Color.White
                  ),
                  modifier = Modifier.testTag("chip_allergy_${allergy.lowercase().take(6)}")
                )
              }
            }
          }
        }
      }

      // 5. Dietary Style Preference
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Dietary & Culinary Preference", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              MedicalConditionCatalog.DIETARY_PREFERENCES.forEach { pref ->
                FilterChip(
                  selected = dietaryPreference == pref,
                  onClick = { dietaryPreference = pref },
                  label = { Text(pref, fontSize = 12.sp) },
                  colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DeepTealPrimary,
                    selectedLabelColor = Color.White
                  )
                )
              }
            }
          }
        }
      }

      // 6. Clinician Nutrient Targets & Clinical Notes
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Clinician-Provided Targets & Limits", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              OutlinedTextField(
                value = dailyCaloriesText,
                onValueChange = { dailyCaloriesText = it },
                label = { Text("Daily Cal Target") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepTealPrimary)
              )
              OutlinedTextField(
                value = maxSodiumText,
                onValueChange = { maxSodiumText = it },
                label = { Text("Max Sodium (mg)") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepTealPrimary)
              )
            }

            OutlinedTextField(
              value = clinicianNotes,
              onValueChange = { clinicianNotes = it },
              label = { Text("Clinician Dietary Notes / Advice") },
              modifier = Modifier.fillMaxWidth(),
              maxLines = 3,
              colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = DeepTealPrimary)
            )
          }
        }
      }

      // 7. Privacy & Explicit Health Data Consent (PRD 11)
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Health Information Consent", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                Text(
                  "I consent to processing health conditions strictly for offline nutrition validation & meal generation.",
                  fontSize = 11.sp,
                  color = TextMuted,
                  lineHeight = 15.sp
                )
              }
              Switch(
                checked = consentGiven,
                onCheckedChange = { consentGiven = it },
                colors = SwitchDefaults.colors(checkedThumbColor = DeepTealPrimary, checkedTrackColor = DeepTealDark)
              )
            }
          }
        }
      }

      // 8. Save Changes Button
      item {
        Button(
          onClick = {
            val parsedAge = ageText.toIntOrNull() ?: 36
            val parsedWeight = weightText.toFloatOrNull() ?: 72f
            val parsedHeight = heightText.toFloatOrNull() ?: 172f
            val parsedCal = dailyCaloriesText.toIntOrNull() ?: 1950
            val parsedSod = maxSodiumText.toIntOrNull() ?: 1500

            val updated = originalProfile.copy(
              name = name.ifBlank { "User" },
              age = parsedAge,
              weightKg = parsedWeight,
              heightCm = parsedHeight,
              gender = gender,
              activityLevel = activityLevel,
              medicalConditions = selectedConditions.toList(),
              allergies = selectedAllergies.toList(),
              dietaryPreference = dietaryPreference,
              dailyCalorieTarget = parsedCal,
              maxSodiumMg = parsedSod,
              clinicianNotes = clinicianNotes,
              consentGiven = consentGiven
            )

            viewModel.saveProfile(updated)
            scope.launch {
              snackbarHostState.showSnackbar("Health profile updated! Safety rules re-synchronized.")
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("save_profile_button"),
          colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
          shape = RoundedCornerShape(14.dp)
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Save & Apply Health Profile", fontSize = 15.sp, fontWeight = FontWeight.Bold)
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
