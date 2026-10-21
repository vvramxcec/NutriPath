package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.HealthProfile
import com.example.data.model.MedicalConditionCatalog
import com.example.ui.theme.AllergyUnselectedBg
import com.example.ui.theme.AllergyUnselectedBorder
import com.example.ui.theme.AllergyUnselectedText
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ChipUnselectedBg
import com.example.ui.theme.ChipUnselectedBorder
import com.example.ui.theme.ChipUnselectedText
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.StatusWarningAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard
import com.example.ui.viewmodel.NutriPathViewModel
import kotlinx.coroutines.launch

import androidx.compose.material.icons.automirrored.filled.ArrowBack

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HealthProfileScreen(
  viewModel: NutriPathViewModel,
  onNavigateBack: (() -> Unit)? = null,
  onLaunchOnboarding: () -> Unit = {},
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
  var selectedMedications by remember(originalProfile) { mutableStateOf(originalProfile.medications.toSet()) }
  var selectedAllergies by remember(originalProfile) { mutableStateOf(originalProfile.allergies.toSet()) }
  var dietaryPreference by remember(originalProfile) { mutableStateOf(originalProfile.dietaryPreference) }
  var nutritionGoal by remember(originalProfile) { mutableStateOf(originalProfile.nutritionGoal) }

  var systolicBpText by remember(originalProfile) { mutableStateOf(originalProfile.systolicBp.toString()) }
  var diastolicBpText by remember(originalProfile) { mutableStateOf(originalProfile.diastolicBp.toString()) }
  var hba1cText by remember(originalProfile) { mutableStateOf(originalProfile.hba1c.toString()) }
  var potassiumText by remember(originalProfile) { mutableStateOf(originalProfile.serumPotassium.toString()) }

  var dailyCaloriesText by remember(originalProfile) { mutableStateOf(originalProfile.dailyCalorieTarget.toString()) }
  var maxSodiumText by remember(originalProfile) { mutableStateOf(originalProfile.maxSodiumMg.toString()) }
  var clinicianNotes by remember(originalProfile) { mutableStateOf(originalProfile.clinicianNotes) }
  var consentGiven by remember(originalProfile) { mutableStateOf(originalProfile.consentGiven) }

  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val textFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DarkCharcoalText,
    unfocusedTextColor = DarkCharcoalText,
    focusedLabelColor = DeepTealDark,
    unfocusedLabelColor = DarkCharcoalText,
    focusedBorderColor = DeepTealPrimary,
    unfocusedBorderColor = CardBorder,
    focusedContainerColor = Color(0xFFFAFCF7),
    unfocusedContainerColor = Color(0xFFFAFCF7),
    cursorColor = DeepTealPrimary
  )

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(LimeYellowBackground)
        .testTag("health_profile_lazy_column"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Header Card with Restart Onboarding button
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                if (onNavigateBack != null) {
                  androidx.compose.material3.IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(36.dp).testTag("profile_back_btn")
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                      contentDescription = "Back",
                      tint = DeepTealDark
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                }
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(LimeYellowBackground),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = DeepTealDark,
                    modifier = Modifier.size(26.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = "Health Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText
                  )
                  Text(
                    text = "Governs clinical safety gates & recipe synthesis",
                    fontSize = 13.sp,
                    color = DarkCharcoalText.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
              onClick = onLaunchOnboarding,
              modifier = Modifier
                .fillMaxWidth()
                .testTag("launch_onboarding_btn"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTealDark),
              border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DeepTealPrimary))
            ) {
              Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Run Step-by-Step Onboarding Wizard", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // 2. Personal Metrics
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = "Personal & Anthropometric Metrics",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )

            OutlinedTextField(
              value = name,
              onValueChange = { name = it },
              label = { Text("Full Name / Label", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
              modifier = Modifier.fillMaxWidth().testTag("profile_name_input"),
              colors = textFieldColors,
              shape = RoundedCornerShape(12.dp)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedTextField(
                value = ageText,
                onValueChange = { ageText = it },
                label = { Text("Age (yrs)", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f).testTag("profile_age_input"),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
              OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Weight (kg)", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f).testTag("profile_weight_input"),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
              OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Height (cm)", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f).testTag("profile_height_input"),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
            }

            Text(
              text = "Activity Level:",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              MedicalConditionCatalog.ACTIVITY_LEVELS.forEach { level ->
                val isSelected = (activityLevel == level)
                FilterChip(
                  selected = isSelected,
                  onClick = { activityLevel = level },
                  label = {
                    Text(
                      text = level,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    containerColor = ChipUnselectedBg,
                    labelColor = ChipUnselectedText,
                    selectedContainerColor = DeepTealPrimary,
                    selectedLabelColor = Color.White
                  ),
                  border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) DeepTealDark else ChipUnselectedBorder
                  ),
                  shape = RoundedCornerShape(10.dp)
                )
              }
            }
          }
        }
      }

      // 3. Clinical Biomarkers (NutriOrion Responsiveness to Patient Profiles)
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = "Clinical Biomarkers (NutriOrion Integration)",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )
            Text(
              text = "Biomarkers drive negative-correlation nutrient tailoring (e.g. higher BP reduces sodium allowance).",
              fontSize = 13.sp,
              color = DarkCharcoalText.copy(alpha = 0.85f)
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedTextField(
                value = systolicBpText,
                onValueChange = { systolicBpText = it },
                label = { Text("Systolic BP", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
              OutlinedTextField(
                value = diastolicBpText,
                onValueChange = { diastolicBpText = it },
                label = { Text("Diastolic BP", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedTextField(
                value = hba1cText,
                onValueChange = { hba1cText = it },
                label = { Text("HbA1c (%)", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
              OutlinedTextField(
                value = potassiumText,
                onValueChange = { potassiumText = it },
                label = { Text("Serum K+ (mEq/L)", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
            }
          }
        }
      }

      // 4. Pharmacological Medications & DNI Safety Gate (NutriOrion)
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Medication, contentDescription = null, tint = DeepTealDark, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Prescribed Medications & DNI Rules",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoalText
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Prevents the 'Healthy Food Trap' by locking out pharmacological contraindications (e.g. Warfarin ↔ Vitamin K).",
              fontSize = 13.sp,
              color = DarkCharcoalText.copy(alpha = 0.85f),
              lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              MedicalConditionCatalog.COMMON_MEDICATIONS.forEach { med ->
                val isSelected = selectedMedications.contains(med)
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    selectedMedications = if (isSelected) {
                      selectedMedications - med
                    } else {
                      if (med == "None") setOf("None") else (selectedMedications - "None") + med
                    }
                  },
                  label = {
                    Text(
                      text = med,
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    containerColor = ChipUnselectedBg,
                    labelColor = ChipUnselectedText,
                    selectedContainerColor = DeepTealPrimary,
                    selectedLabelColor = Color.White
                  ),
                  border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) DeepTealDark else ChipUnselectedBorder
                  ),
                  shape = RoundedCornerShape(10.dp)
                )
              }
            }

            // Show active warnings for selected medications
            val activeWarnings = selectedMedications.mapNotNull { MedicalConditionCatalog.MEDICATION_WARNINGS[it] }
            if (activeWarnings.isNotEmpty()) {
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Active Pharmacological Contraindication Gates:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = StatusRejectedRed
              )
              Spacer(modifier = Modifier.height(6.dp))
              activeWarnings.forEach { warn ->
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFEF2F2))
                    .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                ) {
                  Text(text = "⚠️ $warn", fontSize = 11.sp, color = Color(0xFF991B1B), lineHeight = 15.sp)
                }
              }
            }
          }
        }
      }

      // 5. Medical Conditions
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Diagnosed Medical Conditions",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Selecting a condition activates reviewed deterministic clinical dietary rules for every meal proposal.",
              fontSize = 13.sp,
              color = DarkCharcoalText.copy(alpha = 0.85f),
              lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              MedicalConditionCatalog.AVAILABLE_CONDITIONS.forEach { condition ->
                val isSelected = selectedConditions.contains(condition)
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    selectedConditions = if (isSelected) selectedConditions - condition else selectedConditions + condition
                  },
                  label = {
                    Text(
                      text = condition,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                  },
                  leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White) }
                  } else null,
                  colors = FilterChipDefaults.filterChipColors(
                    containerColor = ChipUnselectedBg,
                    labelColor = ChipUnselectedText,
                    selectedContainerColor = DeepTealPrimary,
                    selectedLabelColor = Color.White
                  ),
                  border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) DeepTealDark else ChipUnselectedBorder
                  ),
                  shape = RoundedCornerShape(10.dp)
                )
              }
            }
          }
        }
      }

      // 6. Food Allergies
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRejectedRed, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Critical Food Allergies",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoalText
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Recipes containing any selected allergen are strictly BLOCKED by Rule-ALLERG-v1.2.",
              fontSize = 13.sp,
              color = StatusRejectedRed,
              fontWeight = FontWeight.SemiBold,
              lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              MedicalConditionCatalog.AVAILABLE_ALLERGIES.forEach { allergy ->
                val isSelected = selectedAllergies.contains(allergy)
                FilterChip(
                  selected = isSelected,
                  onClick = {
                    selectedAllergies = if (isSelected) selectedAllergies - allergy else selectedAllergies + allergy
                  },
                  label = {
                    Text(
                      text = allergy,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    containerColor = AllergyUnselectedBg,
                    labelColor = AllergyUnselectedText,
                    selectedContainerColor = StatusRejectedRed,
                    selectedLabelColor = Color.White
                  ),
                  border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) Color(0xFF7F1D1D) else AllergyUnselectedBorder
                  ),
                  shape = RoundedCornerShape(10.dp)
                )
              }
            }
          }
        }
      }

      // 7. HealthGenie User Excluded Ingredients Manager
      if (originalProfile.excludedIngredients.isNotEmpty()) {
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = "HealthGenie Excluded Ingredients (${originalProfile.excludedIngredients.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoalText
              )
              Text(
                text = "Ingredients you excluded from recommendations. Tap '×' to unblock any item:",
                fontSize = 12.sp,
                color = TextMuted
              )
              Spacer(modifier = Modifier.height(10.dp))

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                originalProfile.excludedIngredients.forEach { item ->
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(8.dp))
                      .background(Color(0xFFFEE2E2))
                      .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(8.dp))
                      .padding(horizontal = 8.dp, vertical = 4.dp)
                  ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(text = item, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF991B1B))
                      Spacer(modifier = Modifier.width(4.dp))
                      Box(
                        modifier = Modifier
                          .size(16.dp)
                          .clip(CircleShape)
                          .background(Color(0xFFFCA5A5))
                          .clickable { viewModel.togglePersistentExclusion(item) },
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove exclusion", tint = Color.White, modifier = Modifier.size(10.dp))
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // 8. Dietary & Culinary Preference
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = "Dietary & Culinary Preference",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Tailors recipe generation to your regional cultural cuisine and culinary choices.",
              fontSize = 13.sp,
              color = DarkCharcoalText.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              MedicalConditionCatalog.DIETARY_PREFERENCES.forEach { pref ->
                val isSelected = (dietaryPreference == pref)
                FilterChip(
                  selected = isSelected,
                  onClick = { dietaryPreference = pref },
                  label = {
                    Text(
                      text = pref,
                      fontSize = 13.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                    )
                  },
                  colors = FilterChipDefaults.filterChipColors(
                    containerColor = ChipUnselectedBg,
                    labelColor = ChipUnselectedText,
                    selectedContainerColor = DeepTealPrimary,
                    selectedLabelColor = Color.White
                  ),
                  border = BorderStroke(
                    width = if (isSelected) 1.5.dp else 1.dp,
                    color = if (isSelected) DeepTealDark else ChipUnselectedBorder
                  ),
                  shape = RoundedCornerShape(10.dp)
                )
              }
            }
          }
        }
      }

      // 9. Clinician Nutrient Targets & Notes
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            Text(
              text = "Clinician-Provided Targets & Limits",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = DarkCharcoalText
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              OutlinedTextField(
                value = dailyCaloriesText,
                onValueChange = { dailyCaloriesText = it },
                label = { Text("Daily Cal Target (kcal)", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
              OutlinedTextField(
                value = maxSodiumText,
                onValueChange = { maxSodiumText = it },
                label = { Text("Max Sodium (mg)", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
                modifier = Modifier.weight(1f),
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp)
              )
            }

            OutlinedTextField(
              value = clinicianNotes,
              onValueChange = { clinicianNotes = it },
              label = { Text("Clinician Dietary Notes / Instructions", fontWeight = FontWeight.Bold, color = DarkCharcoalText) },
              modifier = Modifier.fillMaxWidth(),
              maxLines = 3,
              colors = textFieldColors,
              shape = RoundedCornerShape(12.dp)
            )
          }
        }
      }

      // 10. Privacy & Consent
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Explicit Health Information Consent",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = DarkCharcoalText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "I consent to processing health conditions strictly for nutrition validation & meal generation. Data is saved locally and never shared publicly.",
                  fontSize = 13.sp,
                  color = DarkCharcoalText.copy(alpha = 0.85f),
                  lineHeight = 18.sp
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Switch(
                checked = consentGiven,
                onCheckedChange = { consentGiven = it },
                colors = SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = DeepTealPrimary,
                  uncheckedThumbColor = Color.White,
                  uncheckedTrackColor = Color(0xFFBDC8B8)
                )
              )
            }
          }
        }
      }

      // 11. Save Button
      item {
        Button(
          onClick = {
            val parsedAge = ageText.toIntOrNull() ?: 36
            val parsedWeight = weightText.toFloatOrNull() ?: 72f
            val parsedHeight = heightText.toFloatOrNull() ?: 172f
            val parsedCal = dailyCaloriesText.toIntOrNull() ?: 1950
            val parsedSod = maxSodiumText.toIntOrNull() ?: 1500
            val parsedSys = systolicBpText.toIntOrNull() ?: 135
            val parsedDia = diastolicBpText.toIntOrNull() ?: 85
            val parsedHba1c = hba1cText.toFloatOrNull() ?: 6.8f
            val parsedPot = potassiumText.toFloatOrNull() ?: 4.2f

            val updated = originalProfile.copy(
              name = name.ifBlank { "User" },
              age = parsedAge,
              weightKg = parsedWeight,
              heightCm = parsedHeight,
              gender = gender,
              activityLevel = activityLevel,
              medicalConditions = selectedConditions.toList(),
              medications = selectedMedications.toList(),
              allergies = selectedAllergies.toList(),
              dietaryPreference = dietaryPreference,
              nutritionGoal = nutritionGoal,
              systolicBp = parsedSys,
              diastolicBp = parsedDia,
              hba1c = parsedHba1c,
              serumPotassium = parsedPot,
              dailyCalorieTarget = parsedCal,
              maxSodiumMg = parsedSod,
              clinicianNotes = clinicianNotes,
              consentGiven = consentGiven,
              hasCompletedOnboarding = true
            )

            viewModel.saveProfile(updated)
            scope.launch {
              snackbarHostState.showSnackbar("Health profile & pharmacological safety rules updated successfully!")
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("save_profile_button"),
          colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
          shape = RoundedCornerShape(14.dp),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
          Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.White)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Save & Apply Health Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
