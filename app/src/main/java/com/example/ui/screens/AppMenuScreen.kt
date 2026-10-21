package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowAccent
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.StatusApprovedGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard
import com.example.ui.viewmodel.NutriPathViewModel

@Composable
fun AppMenuScreen(
  viewModel: NutriPathViewModel,
  onNavigateBack: () -> Unit,
  onNavigateToProfile: () -> Unit,
  onNavigateToSafety: () -> Unit,
  onRestartOnboarding: () -> Unit,
  modifier: Modifier = Modifier
) {
  val profile by viewModel.healthProfile.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(LimeYellowBackground)
      .testTag("app_menu_screen")
  ) {
    // Top Bar of the Menu Screen
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(WarmWhiteCard)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onNavigateBack,
        modifier = Modifier
          .size(36.dp)
          .testTag("menu_back_button")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = DeepTealDark
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Text(
        text = "Menu & Clinical Settings",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = DeepTealDark
      )
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Patient Profile Summary Card
      item {
        Card(
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DeepTealPrimary),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = LimeYellowAccent,
                    modifier = Modifier.size(24.dp)
                  )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                  Text(
                    text = if (profile.name.isNotBlank()) profile.name else "Personalized Patient",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkCharcoalText
                  )
                  Text(
                    text = "${profile.age} yrs • ${profile.gender} • ${profile.weightKg.toInt()} kg",
                    fontSize = 12.sp,
                    color = TextMuted
                  )
                }
              }

              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(LimeYellowBackground)
                  .border(1.dp, LimeYellowAccent, RoundedCornerShape(8.dp))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = "${profile.dailyCalorieTarget} kcal",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = DeepTealDark
                )
              }
            }

            if (profile.medicalConditions.isNotEmpty()) {
              Spacer(modifier = Modifier.height(12.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.HealthAndSafety,
                  contentDescription = null,
                  tint = DeepTealPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Conditions: ${profile.medicalConditions.joinToString(", ")}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = DeepTealDark
                )
              }
            }

            if (profile.medications.isNotEmpty()) {
              Spacer(modifier = Modifier.height(6.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Medication,
                  contentDescription = null,
                  tint = DeepTealPrimary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Medications: ${profile.medications.joinToString(", ")}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = TextMuted
                )
              }
            }
          }
        }
      }

      // 2. Navigation Action Cards
      item {
        Text(
          text = "Clinical Navigation",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = DeepTealDark
        )
      }

      item {
        MenuNavigationItem(
          icon = Icons.Default.HealthAndSafety,
          title = "Health Profile",
          subtitle = "Edit age, weight, conditions, medications, allergies, and dietary targets",
          onClick = onNavigateToProfile,
          testTag = "menu_card_profile"
        )
      }

      item {
        MenuNavigationItem(
          icon = Icons.Default.Security,
          title = "Clinical Safety & DNI Rules",
          subtitle = "View Drug-Nutrient Interaction safety gates, contraindications, and ADIME clinical analysis",
          onClick = onNavigateToSafety,
          testTag = "menu_card_safety"
        )
      }

      item {
        MenuNavigationItem(
          icon = Icons.Default.Refresh,
          title = "Re-run Health Setup",
          subtitle = "Restart the onboarding assessment to re-evaluate your clinical parameters",
          onClick = onRestartOnboarding,
          testTag = "menu_card_restart"
        )
      }

      // 3. Clinical Architecture & Nutrition Engine Info
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(LimeYellowBackground),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.VerifiedUser,
                contentDescription = null,
                tint = DeepTealPrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Clinical Engine & Nutrition Validation",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkCharcoalText
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "NutriPath uses deterministic clinical formulas (Mifflin-St Jeor, DASH, ADA, KDIGO) and FDA DailyMed guidelines. The 1,000+ Indian Food Micronutrient Dataset (ICMR-NIN & Anuvaad) powers all recipe generation, micronutrient tracking, and safety validation automatically in the background.",
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MenuNavigationItem(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() }
      .testTag(testTag),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = WarmWhiteCard),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BorderSubtle)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        modifier = Modifier.weight(1f),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(LimeYellowBackground),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DeepTealPrimary,
            modifier = Modifier.size(22.dp)
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DarkCharcoalText
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = subtitle,
            fontSize = 12.sp,
            color = TextMuted,
            lineHeight = 16.sp
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Icon(
        imageVector = Icons.Default.ChevronRight,
        contentDescription = "Navigate",
        tint = DeepTealDark,
        modifier = Modifier.size(22.dp)
      )
    }
  }
}
