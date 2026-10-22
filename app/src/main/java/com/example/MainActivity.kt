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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
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
import com.example.ui.screens.AppMenuScreen
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
  HISTORY("History", Icons.Default.History)
}

enum class ActiveScreen {
  MAIN_TAB,
  MENU,
  PROFILE,
  SAFETY,
  GENERATOR
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
  var activeScreen by remember { mutableStateOf(ActiveScreen.MAIN_TAB) }
  var initialGeneratorMealType by remember { mutableStateOf<String?>(null) }

  // If in onboarding, show full-screen onboarding experience
  if (isOnboardingActive) {
    OnboardingScreen(
      initialProfile = profile,
      viewModel = viewModel,
      onCompleteOnboarding = { finishedProfile ->
        viewModel.completeOnboarding(finishedProfile)
      },
      modifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
    )
    return
  }

  // Handle Android hardware/gesture back press
  BackHandler(enabled = activeScreen != ActiveScreen.MAIN_TAB || currentTab != NutriPathTab.HOME) {
    when (activeScreen) {
      ActiveScreen.PROFILE, ActiveScreen.SAFETY -> {
        activeScreen = ActiveScreen.MENU
      }
      ActiveScreen.MENU, ActiveScreen.GENERATOR -> {
        activeScreen = ActiveScreen.MAIN_TAB
      }
      ActiveScreen.MAIN_TAB -> {
        currentTab = NutriPathTab.HOME
      }
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.statusBars),
    containerColor = LimeYellowBackground,
    // No additional fixed top bar! The 3-dot menu button is embedded directly in screen headers.
    bottomBar = {
      // Bottom navigation only visible on primary daily tabs
      if (activeScreen == ActiveScreen.MAIN_TAB) {
        NavigationBar(
          containerColor = WarmWhiteCard,
          contentColor = DarkCharcoalText,
          windowInsets = WindowInsets.navigationBars,
          modifier = Modifier.testTag("nutripath_bottom_navigation")
        ) {
          NutriPathTab.values().forEach { tab ->
            val isSelected = (currentTab == tab)
            NavigationBarItem(
              selected = isSelected,
              onClick = {
                activeScreen = ActiveScreen.MAIN_TAB
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
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (activeScreen) {
        ActiveScreen.MENU -> {
          AppMenuScreen(
            viewModel = viewModel,
            onNavigateBack = { activeScreen = ActiveScreen.MAIN_TAB },
            onNavigateToProfile = { activeScreen = ActiveScreen.PROFILE },
            onNavigateToSafety = { activeScreen = ActiveScreen.SAFETY },
            onRestartOnboarding = {
              activeScreen = ActiveScreen.MAIN_TAB
              viewModel.restartOnboarding()
            }
          )
        }
        ActiveScreen.GENERATOR -> {
          MealGeneratorScreen(
            viewModel = viewModel,
            initialMealType = initialGeneratorMealType,
            onNavigateBack = { activeScreen = ActiveScreen.MAIN_TAB }
          )
        }
        ActiveScreen.PROFILE -> {
          HealthProfileScreen(
            viewModel = viewModel,
            onNavigateBack = { activeScreen = ActiveScreen.MENU },
            onLaunchOnboarding = {
              activeScreen = ActiveScreen.MAIN_TAB
              viewModel.restartOnboarding()
            }
          )
        }
        ActiveScreen.SAFETY -> {
          SettingsSafetyScreen(
            viewModel = viewModel,
            onNavigateBack = { activeScreen = ActiveScreen.MENU },
            onLaunchOnboarding = {
              activeScreen = ActiveScreen.MAIN_TAB
              viewModel.restartOnboarding()
            }
          )
        }
        ActiveScreen.MAIN_TAB -> {
          AnimatedContent(
            targetState = currentTab,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "tab_transition"
          ) { tab ->
            when (tab) {
              NutriPathTab.HOME -> HomeScreen(
                viewModel = viewModel,
                onNavigateToGenerator = { mealType ->
                  initialGeneratorMealType = mealType
                  activeScreen = ActiveScreen.GENERATOR
                },
                onNavigateToPlanner = {
                  currentTab = NutriPathTab.PLANNER
                },
                onOpenMenu = { activeScreen = ActiveScreen.MENU }
              )
              NutriPathTab.PLANNER -> MealPlannerScreen(
                viewModel = viewModel,
                onNavigateToGenerator = { mealType ->
                  initialGeneratorMealType = mealType
                  activeScreen = ActiveScreen.GENERATOR
                }
              )
              NutriPathTab.HISTORY -> MealHistoryScreen(
                viewModel = viewModel
              )
            }
          }
        }
      }
    }
  }
}
