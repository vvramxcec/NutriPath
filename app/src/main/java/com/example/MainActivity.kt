package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.HealthProfileScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MealGeneratorScreen
import com.example.ui.screens.MealHistoryScreen
import com.example.ui.screens.MealPlannerScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsSafetyScreen
import com.example.ui.theme.DarkCharcoalText
import com.example.ui.theme.DeepTealContainer
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.WarmWhiteCard
import com.example.ui.viewmodel.NutriPathViewModel

enum class NutriPathTab(val title: String, val icon: ImageVector) {
  HOME("Home", Icons.Default.Home),
  PLANNER("Planner", Icons.Default.CalendarMonth),
  PROFILE("Profile", Icons.Default.HealthAndSafety),
  HISTORY("History", Icons.Default.History),
  SETTINGS("Safety", Icons.Default.Security)
}

class MainActivity : ComponentActivity() {

  private val viewModel: NutriPathViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        NutriPathApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun NutriPathApp(viewModel: NutriPathViewModel) {
  val isOnboardingActive by viewModel.isOnboardingActive.collectAsStateWithLifecycle()
  val profile by viewModel.healthProfile.collectAsStateWithLifecycle()

  var currentTab by remember { mutableStateOf(NutriPathTab.HOME) }
  var isGeneratorOpen by remember { mutableStateOf(false) }
  var initialGeneratorMealType by remember { mutableStateOf<String?>(null) }

  // If in onboarding, show full-screen onboarding experience
  if (isOnboardingActive) {
    OnboardingScreen(
      initialProfile = profile,
      onCompleteOnboarding = { finishedProfile ->
        viewModel.completeOnboarding(finishedProfile)
      },
      onSkipToDefaults = {
        viewModel.dismissOnboarding()
      },
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
    )
    return
  }

  // Handle Android hardware/gesture back press
  BackHandler(enabled = isGeneratorOpen || currentTab != NutriPathTab.HOME) {
    if (isGeneratorOpen) {
      isGeneratorOpen = false
    } else {
      currentTab = NutriPathTab.HOME
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.statusBars),
    containerColor = LimeYellowBackground,
    bottomBar = {
      NavigationBar(
        containerColor = WarmWhiteCard,
        contentColor = DarkCharcoalText,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("nutripath_bottom_navigation")
      ) {
        NutriPathTab.values().forEach { tab ->
          val isSelected = (!isGeneratorOpen && currentTab == tab)
          NavigationBarItem(
            selected = isSelected,
            onClick = {
              isGeneratorOpen = false
              currentTab = tab
            },
            icon = {
              Icon(
                imageVector = tab.icon,
                contentDescription = tab.title,
                modifier = Modifier.size(22.dp)
              )
            },
            label = {
              Text(
                text = tab.title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = DeepTealPrimary,
              selectedTextColor = DeepTealPrimary,
              indicatorColor = DeepTealContainer,
              unselectedIconColor = TextMuted,
              unselectedTextColor = TextMuted
            ),
            modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      if (isGeneratorOpen) {
        MealGeneratorScreen(
          viewModel = viewModel,
          initialMealType = initialGeneratorMealType,
          onNavigateBack = { isGeneratorOpen = false }
        )
      } else {
        AnimatedContent(
          targetState = currentTab,
          transitionSpec = { fadeIn() togetherWith fadeOut() },
          label = "screen_transition"
        ) { tab ->
          when (tab) {
            NutriPathTab.HOME -> HomeScreen(
              viewModel = viewModel,
              onNavigateToGenerator = { mealType ->
                initialGeneratorMealType = mealType
                isGeneratorOpen = true
              }
            )
            NutriPathTab.PLANNER -> MealPlannerScreen(
              viewModel = viewModel,
              onNavigateToGenerator = { mealType ->
                initialGeneratorMealType = mealType
                isGeneratorOpen = true
              }
            )
            NutriPathTab.PROFILE -> HealthProfileScreen(
              viewModel = viewModel,
              onLaunchOnboarding = { viewModel.restartOnboarding() }
            )
            NutriPathTab.HISTORY -> MealHistoryScreen(
              viewModel = viewModel
            )
            NutriPathTab.SETTINGS -> SettingsSafetyScreen(
              viewModel = viewModel,
              onLaunchOnboarding = { viewModel.restartOnboarding() }
            )
          }
        }
      }
    }
  }
}
