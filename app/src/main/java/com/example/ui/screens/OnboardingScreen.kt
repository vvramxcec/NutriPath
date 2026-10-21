package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.HealthProfile
import com.example.ui.components.AuthDialog
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LifesumCreamBg
import com.example.ui.theme.LifesumGreenDarkText
import com.example.ui.theme.LifesumGreenLight
import com.example.ui.theme.LifesumLavender
import com.example.ui.theme.LifesumStoneBorder
import com.example.ui.theme.LifesumStoneDark
import com.example.ui.theme.LifesumStoneMuted
import com.example.ui.theme.LifesumVibrantGreen
import com.example.ui.viewmodel.NutriPathViewModel
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Streamlined Lifesum-Style Onboarding Screen
 * Asks ONLY what is medically and clinically required to calculate calories,
 * macronutrients, and safety rules:
 *
 * Step 0: Welcome Hero & Value Proposition
 * Step 1: Weight Goal (Lose Weight, Eat Healthy, Build Muscle)
 * Step 2: Biometrics (Gender, Age, Weight, Height) - directly powers Mifflin-St Jeor BMR & TDEE
 * Step 3: Clinical Health & Safety (Medical Conditions & Allergies) - powers DNI rules & safety gates
 * Step 4: Desired Speed of Change (Calculates daily calorie surplus / deficit)
 * Step 5: Plan Finalization Animation (0% -> 100%)
 * Step 6: Personalized Plan Reveal & Entry (Google / Email / Continue as Guest)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
  initialProfile: HealthProfile,
  onCompleteOnboarding: (HealthProfile) -> Unit,
  onSkipToDefaults: () -> Unit = {},
  viewModel: NutriPathViewModel? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var currentStep by remember { mutableIntStateOf(0) }
  val totalProgressSteps = 4 // Steps 1 to 4 are input steps

  // Required Inputs
  var weightGoal by remember { mutableStateOf(initialProfile.nutritionGoal.ifBlank { "I want to lose weight" }) }
  var gender by remember { mutableStateOf(initialProfile.gender.ifBlank { "Female" }) }
  var age by remember { mutableIntStateOf(if (initialProfile.age > 0) initialProfile.age else 32) }
  var weightKg by remember { mutableFloatStateOf(if (initialProfile.weightKg > 0) initialProfile.weightKg else 70.0f) }
  var heightCm by remember { mutableFloatStateOf(if (initialProfile.heightCm > 0) initialProfile.heightCm else 170.0f) }
  var isLbsUnit by remember { mutableStateOf(false) }

  // Clinical parameters (crucial for NutriPath safety engine)
  var selectedConditions by remember {
    mutableStateOf(
      if (initialProfile.medicalConditions.isNotEmpty()) initialProfile.medicalConditions.toSet()
      else setOf("None / General Wellness")
    )
  }
  var selectedAllergies by remember {
    mutableStateOf(initialProfile.allergies.toSet())
  }
  var speedKgPerWeek by remember { mutableFloatStateOf(0.50f) }

  // Plan generation calculation state (Step 5)
  var calculationProgress by remember { mutableIntStateOf(0) }

  // Auth Dialog state
  var showAuthDialog by remember { mutableStateOf(false) }
  var authErrorMessage by remember { mutableStateOf<String?>(null) }
  var isAuthenticating by remember { mutableStateOf(false) }

  // Helper to compile final profile
  fun buildFinalProfile(): HealthProfile {
    // Mifflin-St Jeor BMR
    val bmr = 10f * weightKg + 6.25f * heightCm - 5f * age + (if (gender.equals("Female", ignoreCase = true)) -161 else 5)
    val activityMultiplier = 1.4f
    val tdee = bmr * activityMultiplier

    // Calorie target based on goal and pace
    val dailyAdjustment = when (weightGoal) {
      "I want to lose weight" -> (speedKgPerWeek * 7700f / 7f)
      "I want more muscle mass" -> -300f
      else -> 0f
    }
    val targetCalories = (tdee - dailyAdjustment).toInt().coerceIn(1350, 3400)

    val isDiabetic = selectedConditions.any { it.contains("Diabetes", ignoreCase = true) }
    val isCkd = selectedConditions.any { it.contains("Kidney", ignoreCase = true) || it.contains("CKD", ignoreCase = true) }

    val proteinMultiplier = when {
      isCkd -> 0.8f
      weightGoal == "I want more muscle mass" -> 1.8f
      else -> 1.3f
    }
    val proteinG = (weightKg * proteinMultiplier).toInt().coerceIn(50, 160)
    val fatG = ((targetCalories * 0.28f) / 9f).toInt().coerceIn(35, 80)
    val carbsG = ((targetCalories - (proteinG * 4) - (fatG * 9)) / 4f).toInt().coerceIn(120, 350)

    val activeConditions = selectedConditions.filter { it != "None / General Wellness" }

    val restrictions = mutableListOf<String>()
    if (isDiabetic) restrictions.add("Low Glycemic Index")
    if (activeConditions.any { it.contains("Hypertension", ignoreCase = true) }) {
      restrictions.add("Low Sodium (< 1500mg)")
    }
    if (activeConditions.any { it.contains("Cholesterol", ignoreCase = true) }) {
      restrictions.add("Low Saturated Fat (< 10g)")
    }

    return initialProfile.copy(
      name = initialProfile.name.ifBlank { "Personalized Patient" },
      age = age,
      gender = gender,
      weightKg = weightKg,
      heightCm = heightCm,
      medicalConditions = activeConditions,
      allergies = selectedAllergies.toList(),
      dietaryRestrictions = restrictions,
      nutritionGoal = weightGoal,
      dailyCalorieTarget = targetCalories,
      proteinTargetG = proteinG,
      carbsTargetG = carbsG,
      fatTargetG = fatG,
      maxSodiumMg = if (activeConditions.any { it.contains("Hypertension", ignoreCase = true) }) 1500 else 2300,
      consentGiven = true,
      hasCompletedOnboarding = true
    )
  }

  // Handle hardware / gesture back button
  BackHandler(enabled = currentStep > 0 && currentStep != 5) {
    if (currentStep == 6) {
      currentStep = 4 // Return to speed of change from final screen
    } else {
      currentStep--
    }
  }

  // Animation trigger for Step 5 (Calculation screen)
  LaunchedEffect(currentStep) {
    if (currentStep == 5) {
      calculationProgress = 0
      while (calculationProgress < 100) {
        delay(20)
        calculationProgress += 2
      }
      delay(300)
      currentStep = 6
    }
  }

  // Auth Dialog Component
  if (showAuthDialog && viewModel != null) {
    AuthDialog(
      isAuthenticating = isAuthenticating,
      onDismiss = { showAuthDialog = false },
      onGoogleSignIn = { ctx ->
        isAuthenticating = true
        viewModel.signInWithGoogle(
          context = ctx,
          onSuccess = {
            isAuthenticating = false
            showAuthDialog = false
            onCompleteOnboarding(buildFinalProfile())
          },
          onError = { err ->
            isAuthenticating = false
            authErrorMessage = err
          }
        )
      },
      onEmailSignIn = { email, pass ->
        isAuthenticating = true
        viewModel.signInWithEmail(
          email = email,
          pass = pass,
          onSuccess = {
            isAuthenticating = false
            showAuthDialog = false
            onCompleteOnboarding(buildFinalProfile())
          },
          onError = { err ->
            isAuthenticating = false
            authErrorMessage = err
          }
        )
      },
      onEmailSignUp = { email, pass ->
        isAuthenticating = true
        viewModel.signUpWithEmail(
          email = email,
          pass = pass,
          onSuccess = {
            isAuthenticating = false
            showAuthDialog = false
            onCompleteOnboarding(buildFinalProfile())
          },
          onError = { err ->
            isAuthenticating = false
            authErrorMessage = err
          }
        )
      }
    )
  }

  // Container
  Surface(
    modifier = modifier
      .fillMaxSize()
      .testTag("streamlined_onboarding_screen"),
    color = if (currentStep == 5) LifesumLavender else LifesumCreamBg
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top Navigation Bar (Shown on input steps 1 to 4)
      if (currentStep in 1..4) {
        LifesumTopBar(
          progress = currentStep / totalProgressSteps.toFloat(),
          onBackClick = { currentStep-- },
          onSkipClick = onSkipToDefaults
        )
      }

      // Step Contents with smooth horizontal transitions
      Box(modifier = Modifier.weight(1f)) {
        AnimatedContent(
          targetState = currentStep,
          transitionSpec = {
            if (targetState > initialState) {
              slideInHorizontally { width -> width / 3 } + fadeIn() togetherWith
                slideOutHorizontally { width -> -width / 3 } + fadeOut()
            } else {
              slideInHorizontally { width -> -width / 3 } + fadeIn() togetherWith
                slideOutHorizontally { width -> width / 3 } + fadeOut()
            }
          },
          label = "onboarding_step_transition"
        ) { step ->
          when (step) {
            0 -> Step0Welcome(
              onGetStarted = { currentStep = 1 },
              onLoginClick = { showAuthDialog = true }
            )
            1 -> Step1Goal(
              selectedGoal = weightGoal,
              onSelectGoal = {
                weightGoal = it
                currentStep = 2
              }
            )
            2 -> Step2Biometrics(
              gender = gender,
              age = age,
              weightKg = weightKg,
              heightCm = heightCm,
              isLbsUnit = isLbsUnit,
              onGenderChanged = { gender = it },
              onAgeChanged = { age = it },
              onWeightChanged = { weightKg = it },
              onHeightChanged = { heightCm = it },
              onUnitToggled = { isLbsUnit = it },
              onNext = { currentStep = 3 }
            )
            3 -> Step3ClinicalSafety(
              selectedConditions = selectedConditions,
              selectedAllergies = selectedAllergies,
              onToggleCondition = { condition ->
                selectedConditions = if (condition == "None / General Wellness") {
                  setOf("None / General Wellness")
                } else {
                  val updated = selectedConditions - "None / General Wellness"
                  if (updated.contains(condition)) updated - condition else updated + condition
                }
              },
              onToggleAllergy = { allergy ->
                selectedAllergies = if (selectedAllergies.contains(allergy)) {
                  selectedAllergies - allergy
                } else {
                  selectedAllergies + allergy
                }
              },
              onNext = { currentStep = 4 }
            )
            4 -> Step4SpeedOfChange(
              speedKgPerWeek = speedKgPerWeek,
              onSpeedChanged = { speedKgPerWeek = it },
              onBuildPlan = { currentStep = 5 }
            )
            5 -> Step5Calculation(
              progress = calculationProgress
            )
            6 -> Step6PlanReady(
              finalProfile = buildFinalProfile(),
              onGoogleSignIn = {
                if (viewModel != null) {
                  viewModel.signInWithGoogle(
                    context = context,
                    onSuccess = { onCompleteOnboarding(buildFinalProfile()) },
                    onError = { authErrorMessage = it }
                  )
                } else {
                  onCompleteOnboarding(buildFinalProfile())
                }
              },
              onEmailSignIn = { showAuthDialog = true },
              onContinueAsGuest = {
                onCompleteOnboarding(buildFinalProfile())
              }
            )
          }
        }
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Top Bar Component
// ---------------------------------------------------------------------------
@Composable
private fun LifesumTopBar(
  progress: Float,
  onBackClick: () -> Unit,
  onSkipClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(
      onClick = onBackClick,
      modifier = Modifier
        .size(40.dp)
        .testTag("onboarding_back_button")
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        tint = LifesumStoneDark,
        modifier = Modifier.size(24.dp)
      )
    }

    Spacer(modifier = Modifier.width(8.dp))

    LinearProgressIndicator(
      progress = { progress.coerceIn(0.1f, 1f) },
      modifier = Modifier
        .weight(1f)
        .height(4.dp)
        .clip(RoundedCornerShape(2.dp)),
      color = LifesumStoneDark,
      trackColor = LifesumStoneBorder
    )

    Spacer(modifier = Modifier.width(8.dp))

    TextButton(
      onClick = onSkipClick,
      modifier = Modifier.testTag("onboarding_skip_button")
    ) {
      Text(
        text = "Skip",
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = LifesumStoneMuted
      )
    }
  }
}

// ---------------------------------------------------------------------------
// Step 0: Welcome / Inspiration
// ---------------------------------------------------------------------------
@Composable
private fun Step0Welcome(
  onGetStarted: () -> Unit,
  onLoginClick: () -> Unit
) {
  val context = LocalContext.current

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(modifier = Modifier.height(12.dp))

      // Brand Pill
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(Color.White)
          .border(1.dp, LifesumStoneBorder, RoundedCornerShape(20.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp)
      ) {
        Box(
          modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(LifesumVibrantGreen)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "NutriPath Clinical Nutrition",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = LifesumStoneDark
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Hero Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(260.dp)
          .clip(RoundedCornerShape(28.dp))
          .background(Color.White)
          .border(1.dp, LifesumStoneBorder, RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.BottomStart
      ) {
        AsyncImage(
          model = ImageRequest.Builder(context)
            .data("file:///android_asset/onboarding_hero.png")
            .crossfade(true)
            .build(),
          contentDescription = "Nutrition and Health",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )

        Box(
          modifier = Modifier
            .padding(16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.94f))
            .border(1.dp, LifesumStoneBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = null,
              tint = LifesumVibrantGreen,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Personalized clinical guidance",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = LifesumStoneDark
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      Text(
        text = "Nutrition is your superpower for change",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LifesumStoneDark,
        textAlign = TextAlign.Center,
        lineHeight = 34.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Scientifically tailored recipes and safety rules built around your body and health conditions.",
        fontSize = 15.sp,
        color = LifesumStoneMuted,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 28.dp)
    ) {
      Button(
        onClick = onGetStarted,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = LifesumVibrantGreen,
          contentColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("welcome_get_started_btn")
      ) {
        Text(
          text = "GET STARTED",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      TextButton(
        onClick = onLoginClick,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("welcome_login_btn")
      ) {
        Text(
          text = "I already have an account • Sign In",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = LifesumStoneDark
        )
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Step 1: Weight Goal
// ---------------------------------------------------------------------------
@Composable
private fun Step1Goal(
  selectedGoal: String,
  onSelectGoal: (String) -> Unit
) {
  val options = listOf(
    "I want to lose weight" to "Deficit-based calorie pacing with high satiety Indian foods",
    "Fine, but want to eat healthy" to "Maintenance target with metabolic and micronutrient balance",
    "I want more muscle mass" to "Hypertrophy-supportive surplus with elevated protein targets"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "How do you feel about your weight?",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = LifesumStoneDark,
        lineHeight = 32.sp
      )

      Spacer(modifier = Modifier.height(28.dp))

      options.forEach { (title, desc) ->
        val isSelected = (selectedGoal == title)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelectGoal(title) }
            .testTag("goal_${title.replace(" ", "_").lowercase()}"),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) LifesumVibrantGreen else Color.White
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isSelected) LifesumVibrantGreen else LifesumStoneBorder)
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 3.dp else 1.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Text(
              text = title,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) Color.White else LifesumStoneDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = desc,
              fontSize = 12.sp,
              color = if (isSelected) Color.White.copy(alpha = 0.9f) else LifesumStoneMuted
            )
          }
        }
        Spacer(modifier = Modifier.height(14.dp))
      }
    }

    Text(
      text = "Your goal directly determines your daily energy target and macro ratios.",
      fontSize = 13.sp,
      color = LifesumStoneMuted,
      textAlign = TextAlign.Center,
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    )
  }
}

// ---------------------------------------------------------------------------
// Step 2: Biometrics (Gender, Age, Weight, Height)
// ---------------------------------------------------------------------------
@Composable
private fun Step2Biometrics(
  gender: String,
  age: Int,
  weightKg: Float,
  heightCm: Float,
  isLbsUnit: Boolean,
  onGenderChanged: (String) -> Unit,
  onAgeChanged: (Int) -> Unit,
  onWeightChanged: (Float) -> Unit,
  onHeightChanged: (Float) -> Unit,
  onUnitToggled: (Boolean) -> Unit,
  onNext: () -> Unit
) {
  val displayWeight = if (isLbsUnit) weightKg * 2.20462f else weightKg
  val unitLabel = if (isLbsUnit) "lbs" else "kg"

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Your body metrics",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = LifesumStoneDark,
        lineHeight = 32.sp
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Used exclusively for Mifflin-St Jeor metabolic rate calculations.",
        fontSize = 13.sp,
        color = LifesumStoneMuted
      )

      Spacer(modifier = Modifier.height(24.dp))

      // 1. Biological Gender
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        val isFemale = gender.equals("Female", ignoreCase = true)
        val isMale = gender.equals("Male", ignoreCase = true)

        Box(
          modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isFemale) LifesumVibrantGreen else Color.White)
            .border(1.dp, if (isFemale) LifesumVibrantGreen else LifesumStoneBorder, RoundedCornerShape(14.dp))
            .clickable { onGenderChanged("Female") }
            .testTag("gender_female"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Female",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFemale) Color.White else LifesumStoneDark
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .height(54.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isMale) LifesumVibrantGreen else Color.White)
            .border(1.dp, if (isMale) LifesumVibrantGreen else LifesumStoneBorder, RoundedCornerShape(14.dp))
            .clickable { onGenderChanged("Male") }
            .testTag("gender_male"),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Male",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isMale) Color.White else LifesumStoneDark
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 2. Age Stepper
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(LifesumStoneBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(text = "Age", fontSize = 12.sp, color = LifesumStoneMuted)
            Text(text = "$age years", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LifesumStoneDark)
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = { if (age > 16) onAgeChanged(age - 1) },
              shape = CircleShape,
              modifier = Modifier.size(42.dp)
            ) {
              Text("−", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LifesumStoneDark)
            }
            OutlinedButton(
              onClick = { if (age < 95) onAgeChanged(age + 1) },
              shape = CircleShape,
              modifier = Modifier.size(42.dp)
            ) {
              Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LifesumStoneDark)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 3. Weight Card with Unit Switch
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(LifesumStoneBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Current Weight", fontSize = 12.sp, color = LifesumStoneMuted)
              Text(
                text = "${String.format(Locale.US, "%.1f", displayWeight)} $unitLabel",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LifesumStoneDark
              )
            }

            // kg / lbs switcher
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(LifesumCreamBg)
                .padding(3.dp)
            ) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (!isLbsUnit) Color.White else Color.Transparent)
                  .clickable { onUnitToggled(false) }
                  .padding(horizontal = 12.dp, vertical = 4.dp)
              ) {
                Text(text = "kg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isLbsUnit) LifesumStoneDark else LifesumStoneMuted)
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isLbsUnit) Color.White else Color.Transparent)
                  .clickable { onUnitToggled(true) }
                  .padding(horizontal = 12.dp, vertical = 4.dp)
              ) {
                Text(text = "lbs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isLbsUnit) LifesumStoneDark else LifesumStoneMuted)
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Slider(
            value = weightKg,
            onValueChange = onWeightChanged,
            valueRange = 35f..160f,
            colors = SliderDefaults.colors(
              thumbColor = LifesumVibrantGreen,
              activeTrackColor = LifesumVibrantGreen,
              inactiveTrackColor = LifesumStoneBorder
            )
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      // 4. Height Card
      Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(LifesumStoneBorder)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Text(text = "Height", fontSize = 12.sp, color = LifesumStoneMuted)
          Text(text = "${heightCm.toInt()} cm", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LifesumStoneDark)

          Spacer(modifier = Modifier.height(6.dp))

          Slider(
            value = heightCm,
            onValueChange = onHeightChanged,
            valueRange = 120f..220f,
            colors = SliderDefaults.colors(
              thumbColor = LifesumVibrantGreen,
              activeTrackColor = LifesumVibrantGreen,
              inactiveTrackColor = LifesumStoneBorder
            )
          )
        }
      }
    }

    Button(
      onClick = onNext,
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = LifesumVibrantGreen,
        contentColor = Color.White
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(top = 24.dp)
        .testTag("biometrics_next_btn")
    ) {
      Text("NEXT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
  }
}

// ---------------------------------------------------------------------------
// Step 3: Clinical Health & Safety (Conditions & Allergies)
// ---------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3ClinicalSafety(
  selectedConditions: Set<String>,
  selectedAllergies: Set<String>,
  onToggleCondition: (String) -> Unit,
  onToggleAllergy: (String) -> Unit,
  onNext: () -> Unit
) {
  val clinicalConditions = listOf(
    "Diabetes Type 2",
    "Hypertension",
    "Chronic Kidney Disease (CKD)",
    "High Cholesterol / Hyperlipidemia",
    "Celiac Disease",
    "None / General Wellness"
  )

  val allergies = listOf(
    "Peanuts", "Tree Nuts", "Milk / Dairy", "Eggs", "Wheat / Gluten", "Fish", "Shellfish"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Medical conditions & allergies",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = LifesumStoneDark,
        lineHeight = 32.sp
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "NutriPath uses these to enforce clinical safety limits (DASH sodium, ADA glycemic index, KDIGO renal thresholds) and filter allergens.",
        fontSize = 13.sp,
        color = LifesumStoneMuted,
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Medical Conditions",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = LifesumStoneDark
      )
      Spacer(modifier = Modifier.height(10.dp))

      clinicalConditions.forEach { condition ->
        val isSelected = selectedConditions.contains(condition)
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggleCondition(condition) }
            .testTag("condition_${condition.replace(" ", "_").lowercase()}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) LifesumVibrantGreen else Color.White
          ),
          border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(if (isSelected) LifesumVibrantGreen else LifesumStoneBorder)
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color.White else LifesumCreamBg)
                .border(1.dp, if (isSelected) Color.White else LifesumStoneBorder, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = null,
                  tint = LifesumGreenDarkText,
                  modifier = Modifier.size(14.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
              text = condition,
              fontSize = 15.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isSelected) Color.White else LifesumStoneDark
            )
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      Spacer(modifier = Modifier.height(20.dp))

      Text(
        text = "Food Allergies (Zero-tolerance exclusions)",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = LifesumStoneDark
      )
      Spacer(modifier = Modifier.height(10.dp))

      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        allergies.forEach { allergy ->
          val isSelected = selectedAllergies.contains(allergy)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) LifesumVibrantGreen else Color.White)
              .border(1.dp, if (isSelected) LifesumVibrantGreen else LifesumStoneBorder, RoundedCornerShape(12.dp))
              .clickable { onToggleAllergy(allergy) }
              .padding(horizontal = 14.dp, vertical = 10.dp)
              .testTag("allergy_${allergy.replace(" ", "_").lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = allergy,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              color = if (isSelected) Color.White else LifesumStoneDark
            )
          }
        }
      }
    }

    Button(
      onClick = onNext,
      shape = RoundedCornerShape(16.dp),
      colors = ButtonDefaults.buttonColors(
        containerColor = LifesumVibrantGreen,
        contentColor = Color.White
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(top = 24.dp)
        .testTag("clinical_next_btn")
    ) {
      Text("NEXT", fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
  }
}

// ---------------------------------------------------------------------------
// Step 4: Desired Speed of Change
// ---------------------------------------------------------------------------
@Composable
private fun Step4SpeedOfChange(
  speedKgPerWeek: Float,
  onSpeedChanged: (Float) -> Unit,
  onBuildPlan: () -> Unit
) {
  val descriptor = when {
    speedKgPerWeek < 0.35f -> "Lasting impact • Mild deficit"
    speedKgPerWeek < 0.70f -> "Steady momentum • Recommended"
    else -> "Accelerated pace • Intensive"
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(24.dp),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Desired speed of change?",
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        color = LifesumStoneDark,
        lineHeight = 32.sp
      )

      Spacer(modifier = Modifier.height(64.dp))

      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = String.format(Locale.US, "%.2f kg/week", speedKgPerWeek),
          fontSize = 40.sp,
          fontWeight = FontWeight.ExtraBold,
          color = LifesumStoneDark
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = descriptor,
          fontSize = 16.sp,
          fontWeight = FontWeight.SemiBold,
          color = LifesumGreenDarkText
        )

        Spacer(modifier = Modifier.height(36.dp))

        Slider(
          value = speedKgPerWeek,
          onValueChange = onSpeedChanged,
          valueRange = 0.10f..1.00f,
          steps = 8,
          modifier = Modifier.fillMaxWidth(),
          colors = SliderDefaults.colors(
            thumbColor = LifesumGreenDarkText,
            activeTrackColor = LifesumGreenDarkText,
            inactiveTrackColor = LifesumStoneBorder
          )
        )
      }
    }

    Column {
      Text(
        text = "A balanced pace ensures sustainable fat loss without metabolic slowdown or muscle loss.",
        fontSize = 13.sp,
        color = LifesumStoneMuted,
        textAlign = TextAlign.Center,
        lineHeight = 18.sp,
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 20.dp)
      )

      Button(
        onClick = onBuildPlan,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = LifesumVibrantGreen,
          contentColor = Color.White
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(56.dp)
          .testTag("build_my_plan_btn")
      ) {
        Text(
          text = "BUILD MY PLAN",
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}

// ---------------------------------------------------------------------------
// Step 5: Calculation Screen (0% -> 100%)
// ---------------------------------------------------------------------------
@Composable
private fun Step5Calculation(progress: Int) {
  val status = when {
    progress < 35 -> "Calculating BMR with Mifflin-St Jeor..."
    progress < 70 -> "Applying ICMR-NIN micronutrient standards..."
    progress < 95 -> "Configuring clinical safety & DNI rules..."
    else -> "Finalizing your plan"
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Spacer(modifier = Modifier.height(60.dp))

    Text(
      text = "$progress%",
      fontSize = 84.sp,
      fontWeight = FontWeight.Black,
      color = LifesumStoneDark,
      letterSpacing = (-2).sp
    )

    Text(
      text = status,
      fontSize = 16.sp,
      fontWeight = FontWeight.Medium,
      color = LifesumStoneDark,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(bottom = 40.dp)
    )
  }
}

// ---------------------------------------------------------------------------
// Step 6: Plan Ready & Entry
// ---------------------------------------------------------------------------
@Composable
private fun Step6PlanReady(
  finalProfile: HealthProfile,
  onGoogleSignIn: () -> Unit,
  onEmailSignIn: () -> Unit,
  onContinueAsGuest: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(24.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Your personalized plan is ready",
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        color = LifesumStoneDark,
        textAlign = TextAlign.Center,
        lineHeight = 34.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Create an account to begin progress or start immediately as a guest",
        fontSize = 14.sp,
        color = LifesumStoneMuted,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Summary Card
      Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(LifesumStoneBorder)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "Daily Calorie Target", fontSize = 12.sp, color = LifesumStoneMuted)
              Text(
                text = "${finalProfile.dailyCalorieTarget} kcal",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LifesumStoneDark
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(LifesumGreenLight)
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = "Clinical Certified",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = LifesumGreenDarkText
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            MacroPill("Protein", "${finalProfile.proteinTargetG}g")
            MacroPill("Carbs", "${finalProfile.carbsTargetG}g")
            MacroPill("Fat", "${finalProfile.fatTargetG}g")
            MacroPill("Sodium", "<${finalProfile.maxSodiumMg}mg")
          }

          if (finalProfile.medicalConditions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.HealthAndSafety,
                contentDescription = null,
                tint = LifesumGreenDarkText,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Active Rules: ${finalProfile.medicalConditions.joinToString(", ")}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = LifesumStoneDark
              )
            }
          }
        }
      }
    }

    // Action Buttons
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 24.dp)
    ) {
      Button(
        onClick = onGoogleSignIn,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LifesumStoneDark),
        border = BorderStroke(1.dp, LifesumStoneBorder),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("plan_ready_google_btn")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AccountCircle, contentDescription = null, tint = DeepTealPrimary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = onEmailSignIn,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = LifesumStoneDark),
        border = BorderStroke(1.dp, LifesumStoneBorder),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("plan_ready_email_btn")
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Email, contentDescription = null, tint = LifesumStoneDark, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(text = "Continue with Email", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = onContinueAsGuest,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = LifesumVibrantGreen, contentColor = Color.White),
        modifier = Modifier
          .fillMaxWidth()
          .height(54.dp)
          .testTag("plan_ready_guest_btn")
      ) {
        Text(text = "Continue as Guest", fontSize = 15.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
private fun MacroPill(label: String, value: String) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(LifesumCreamBg)
      .padding(horizontal = 10.dp, vertical = 6.dp)
  ) {
    Text(text = label, fontSize = 10.sp, color = LifesumStoneMuted)
    Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = LifesumStoneDark)
  }
}
