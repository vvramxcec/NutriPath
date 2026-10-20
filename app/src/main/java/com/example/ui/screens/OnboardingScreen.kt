package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.DeepTealContainer
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowAccent
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.StatusRejectedRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
  initialProfile: HealthProfile,
  onCompleteOnboarding: (HealthProfile) -> Unit,
  onSkipToDefaults: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var currentStep by remember { mutableIntStateOf(1) }
  val totalSteps = 5

  var name by remember { mutableStateOf(initialProfile.name) }
  var ageText by remember { mutableStateOf(initialProfile.age.toString()) }
  var weightText by remember { mutableStateOf(initialProfile.weightKg.toString()) }
  var heightText by remember { mutableStateOf(initialProfile.heightCm.toString()) }
  var gender by remember { mutableStateOf(initialProfile.gender) }
  var activityLevel by remember { mutableStateOf(initialProfile.activityLevel) }

  var selectedConditions by remember { mutableStateOf(initialProfile.medicalConditions.toSet()) }
  var selectedAllergies by remember { mutableStateOf(initialProfile.allergies.toSet()) }
  var dietaryPreference by remember { mutableStateOf(initialProfile.dietaryPreference) }
  var nutritionGoal by remember { mutableStateOf(initialProfile.nutritionGoal) }
  var consentGiven by remember { mutableStateOf(true) }

  val textFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DarkCharcoalText,
    unfocusedTextColor = DarkCharcoalText,
    focusedLabelColor = DeepTealDark,
    unfocusedLabelColor = DarkCharcoalText.copy(alpha = 0.8f),
    focusedBorderColor = DeepTealPrimary,
    unfocusedBorderColor = CardBorder,
    focusedContainerColor = Color(0xFFFAFCF7),
    unfocusedContainerColor = Color(0xFFFAFCF7),
    cursorColor = DeepTealPrimary
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(LimeYellowBackground)
      .testTag("onboarding_screen")
  ) {
    // 1. Top Bar with Progress
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .background(WarmWhiteCard)
        .padding(horizontal = 20.dp, vertical = 14.dp)
        .border(0.dp, Color.Transparent)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (currentStep > 1) {
          IconButton(
            onClick = { currentStep-- },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = DeepTealDark
            )
          }
        } else {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(DeepTealPrimary),
              contentAlignment = Alignment.Center
            ) {
              Text("NP", color = LimeYellowAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("NutriPath", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DeepTealDark)
          }
        }

        Text(
          text = "Step $currentStep of $totalSteps",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = DeepTealDark
        )

        TextButton(
          onClick = {
            onSkipToDefaults()
          }
        ) {
          Text("Skip", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      LinearProgressIndicator(
        progress = { currentStep / totalSteps.toFloat() },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = DeepTealPrimary,
        trackColor = DeepTealContainer
      )
    }

    // 2. Animated Step Content
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      AnimatedContent(
        targetState = currentStep,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "onboarding_step_transition"
      ) { step ->
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          when (step) {
            // STEP 1: Welcome & Clinical Vision
            1 -> {
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Box(
                    modifier = Modifier
                      .size(80.dp)
                      .clip(RoundedCornerShape(22.dp))
                      .background(DeepTealPrimary),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.HealthAndSafety,
                      contentDescription = null,
                      tint = LimeYellowAccent,
                      modifier = Modifier.size(44.dp)
                    )
                  }

                  Spacer(modifier = Modifier.height(18.dp))

                  Text(
                    text = "Welcome to NutriPath",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText,
                    textAlign = TextAlign.Center
                  )

                  Spacer(modifier = Modifier.height(8.dp))

                  Text(
                    text = "Personalized clinical nutrition tailored to your medical health before recommending food.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium
                  )

                  Spacer(modifier = Modifier.height(24.dp))

                  PillarRow(
                    icon = Icons.Default.Security,
                    title = "Independent Safety Validator",
                    description = "Clinical rules strictly screen for allergens, diabetes glycemic load, hypertension sodium, and CKD protein."
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  PillarRow(
                    icon = Icons.Default.Verified,
                    title = "Verified Food Composition",
                    description = "Nutrients deterministically calculated from authentic ICMR-NIN & USDA food databases."
                  )

                  Spacer(modifier = Modifier.height(14.dp))

                  PillarRow(
                    icon = Icons.Default.Restaurant,
                    title = "Kerala & Indian Specialties",
                    description = "Authentic dishes with Kerala Red Matta rice, Moong dal, Thoran, Avial, and Ragi formatted to your health goals."
                  )
                }
              }
            }

            // STEP 2: Demographics & Body Metrics
            2 -> {
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(22.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LimeYellowBackground),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Person, contentDescription = null, tint = DeepTealDark)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text("Personal Demographics", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                      Text("Used to calculate your daily metabolic energy needs", fontSize = 12.sp, color = TextMuted)
                    }
                  }

                  OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("What should we call you?", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name_input"),
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
                      label = { Text("Age (yrs)", fontWeight = FontWeight.SemiBold) },
                      modifier = Modifier.weight(1f).testTag("onboarding_age_input"),
                      colors = textFieldColors,
                      shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                      value = weightText,
                      onValueChange = { weightText = it },
                      label = { Text("Weight (kg)", fontWeight = FontWeight.SemiBold) },
                      modifier = Modifier.weight(1f).testTag("onboarding_weight_input"),
                      colors = textFieldColors,
                      shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                      value = heightText,
                      onValueChange = { heightText = it },
                      label = { Text("Height (cm)", fontWeight = FontWeight.SemiBold) },
                      modifier = Modifier.weight(1f).testTag("onboarding_height_input"),
                      colors = textFieldColors,
                      shape = RoundedCornerShape(12.dp)
                    )
                  }

                  Text("Gender / Sex:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Male", "Female", "Other").forEach { g ->
                      val isSelected = (gender == g)
                      FilterChip(
                        selected = isSelected,
                        onClick = { gender = g },
                        label = { Text(g, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold) },
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

                  Text("Daily Activity Level:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                  FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    MedicalConditionCatalog.ACTIVITY_LEVELS.forEach { level ->
                      val isSelected = (activityLevel == level)
                      FilterChip(
                        selected = isSelected,
                        onClick = { activityLevel = level },
                        label = { Text(level, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold) },
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

            // STEP 3: Medical Conditions
            3 -> {
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(22.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LimeYellowBackground),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.HealthAndSafety, contentDescription = null, tint = DeepTealDark)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text("Medical Conditions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                      Text("Select all that apply. Select None if in good health.", fontSize = 12.sp, color = TextMuted)
                    }
                  }

                  Spacer(modifier = Modifier.height(16.dp))

                  MedicalConditionCatalog.AVAILABLE_CONDITIONS.forEach { condition ->
                    val isSelected = selectedConditions.contains(condition)
                    val desc = MedicalConditionCatalog.CONDITION_DESCRIPTIONS[condition] ?: ""

                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) DeepTealContainer.copy(alpha = 0.5f) else Color(0xFFF9FAF6))
                        .border(
                          1.5.dp,
                          if (isSelected) DeepTealPrimary else ChipUnselectedBorder,
                          RoundedCornerShape(12.dp)
                        )
                        .clickable {
                          selectedConditions = if (isSelected) selectedConditions - condition else selectedConditions + condition
                        }
                        .padding(12.dp)
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Box(
                          modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) DeepTealPrimary else Color.Transparent)
                            .border(1.5.dp, if (isSelected) DeepTealPrimary else CardBorder, CircleShape),
                          contentAlignment = Alignment.Center
                        ) {
                          if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                          }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                          Text(condition, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                          Spacer(modifier = Modifier.height(2.dp))
                          Text(desc, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)
                        }
                      }
                    }
                  }
                }
              }
            }

            // STEP 4: Food Allergies & Intolerances
            4 -> {
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(22.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AllergyUnselectedBg),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRejectedRed)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text("Critical Food Allergies", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                      Text("Checked items are strictly prohibited by safety checks", fontSize = 12.sp, color = StatusRejectedRed)
                    }
                  }

                  Spacer(modifier = Modifier.height(16.dp))

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
                        leadingIcon = if (isSelected) {
                          { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White) }
                        } else null,
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

                  Spacer(modifier = Modifier.height(16.dp))

                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(12.dp))
                      .background(Color(0xFFFEF2F2))
                      .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                      .padding(12.dp)
                  ) {
                    Text(
                      text = if (selectedAllergies.isEmpty()) {
                        "✓ No allergies declared. Full ingredient spectrum permitted."
                      } else {
                        "⚠️ Rule-ALLERG-v1.2 active: Recipes with ${selectedAllergies.joinToString(", ")} will be automatically blocked."
                      },
                      fontSize = 12.sp,
                      color = if (selectedAllergies.isEmpty()) StatusApprovedGreen else StatusRejectedRed,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }
            }

            // STEP 5: Diet Preference, Goals & Consent
            5 -> {
              Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CardBorder)),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(
                  modifier = Modifier.padding(22.dp),
                  verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                      modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(LimeYellowBackground),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(Icons.Default.Favorite, contentDescription = null, tint = DeepTealDark)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                      Text("Diet Preference & Consent", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                      Text("Final step before generating your personalized meals", fontSize = 12.sp, color = TextMuted)
                    }
                  }

                  Text("Preferred Cuisine / Diet Style:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                  FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    MedicalConditionCatalog.DIETARY_PREFERENCES.forEach { pref ->
                      val isSelected = (dietaryPreference == pref)
                      FilterChip(
                        selected = isSelected,
                        onClick = { dietaryPreference = pref },
                        label = { Text(pref, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold) },
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

                  Text("Primary Nutrition Goal:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
                  FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    MedicalConditionCatalog.NUTRITION_GOALS.forEach { goal ->
                      val isSelected = (nutritionGoal == goal)
                      FilterChip(
                        selected = isSelected,
                        onClick = { nutritionGoal = goal },
                        label = { Text(goal, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold) },
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

                  // Privacy Consent Box
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(14.dp))
                      .background(Color(0xFFF3F8F2))
                      .border(1.dp, Color(0xFFD4E2D0), RoundedCornerShape(14.dp))
                      .padding(14.dp)
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = "Health Data Privacy Consent",
                          fontSize = 13.sp,
                          fontWeight = FontWeight.Bold,
                          color = DarkCharcoalText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                          text = "I consent to processing health conditions strictly for offline meal safety validation.",
                          fontSize = 11.sp,
                          color = TextMuted,
                          lineHeight = 15.sp
                        )
                      }
                      Spacer(modifier = Modifier.width(10.dp))
                      Switch(
                        checked = consentGiven,
                        onCheckedChange = { consentGiven = it },
                        colors = SwitchDefaults.colors(
                          checkedThumbColor = Color.White,
                          checkedTrackColor = DeepTealPrimary
                        )
                      )
                    }
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
        }
      }
    }

    // 3. Bottom Action Buttons
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .background(WarmWhiteCard)
        .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
      if (currentStep < totalSteps) {
        Button(
          onClick = { currentStep++ },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("onboarding_next_step_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
          shape = RoundedCornerShape(14.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (currentStep == 1) "Start Health Setup" else "Continue to Next Step",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      } else {
        Button(
          onClick = {
            val parsedAge = ageText.toIntOrNull() ?: 36
            val parsedWeight = weightText.toFloatOrNull() ?: 72f
            val parsedHeight = heightText.toFloatOrNull() ?: 172f

            // Auto calculate calorie target based on Harris-Benedict/Mifflin
            val bmr = 10f * parsedWeight + 6.25f * parsedHeight - 5f * parsedAge + (if (gender == "Female") -161 else 5)
            val activityMultiplier = when (activityLevel) {
              "Sedentary" -> 1.2f
              "Lightly Active" -> 1.375f
              "Moderately Active" -> 1.55f
              else -> 1.725f
            }
            val calculatedCalories = (bmr * activityMultiplier).toInt().coerceIn(1400, 3200)

            val finishedProfile = initialProfile.copy(
              name = name.ifBlank { "User" },
              age = parsedAge,
              weightKg = parsedWeight,
              heightCm = parsedHeight,
              gender = gender,
              activityLevel = activityLevel,
              medicalConditions = selectedConditions.toList(),
              allergies = selectedAllergies.toList(),
              dietaryPreference = dietaryPreference,
              nutritionGoal = nutritionGoal,
              dailyCalorieTarget = calculatedCalories,
              consentGiven = consentGiven,
              hasCompletedOnboarding = true
            )

            onCompleteOnboarding(finishedProfile)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("onboarding_complete_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = DeepTealPrimary),
          shape = RoundedCornerShape(14.dp),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LimeYellowAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Complete & Launch NutriPath",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PillarRow(
  icon: ImageVector,
  title: String,
  description: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(LimeYellowBackground),
      contentAlignment = Alignment.Center
    ) {
      Icon(imageVector = icon, contentDescription = null, tint = DeepTealDark, modifier = Modifier.size(20.dp))
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkCharcoalText)
      Spacer(modifier = Modifier.height(2.dp))
      Text(text = description, fontSize = 12.sp, color = TextMuted, lineHeight = 16.sp)
    }
  }
}
