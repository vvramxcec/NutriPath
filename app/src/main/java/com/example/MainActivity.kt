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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.DeepTealPrimary
import com.example.ui.theme.LimeYellowAccent
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
  var showTopMenu by remember { mutableStateOf(false) }

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
  BackHandler(enabled = activeScreen != ActiveScreen.MAIN_TAB || currentTab != NutriPathTab.HOME) {
    if (activeScreen != ActiveScreen.MAIN_TAB) {
      activeScreen = ActiveScreen.MAIN_TAB
    } else {
      currentTab = NutriPathTab.HOME
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .windowInsetsPadding(WindowInsets.statusBars),
    containerColor = LimeYellowBackground,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(WarmWhiteCard)
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (activeScreen != ActiveScreen.MAIN_TAB) {
            IconButton(
              onClick = { activeScreen = ActiveScreen.MAIN_TAB },
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DeepTealDark
              )
            }
            Spacer(modifier = Modifier.width(4.dp))
          } else {
            Box(
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DeepTealPrimary),
              contentAlignment = Alignment.Center
            ) {
              Text("NP", color = LimeYellowAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
          }

          Text(
            text = when (activeScreen) {
              ActiveScreen.PROFILE -> "Health Profile"
              ActiveScreen.SAFETY -> "Clinical Safety & DNI"
              ActiveScreen.GENERATOR -> "Meal Generator"
              ActiveScreen.MAIN_TAB -> "NutriPath"
            },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = DeepTealDark
          )
        }

        // Top-Right Ellipsis Menu Button
        Box {
          IconButton(
            onClick = { showTopMenu = !showTopMenu },
            modifier = Modifier.testTag("top_ellipsis_menu_button")
          ) {
            Icon(
              imageVector = Icons.Default.MoreVert,
              contentDescription = "Menu",
              tint = DeepTealDark
            )
          }

          DropdownMenu(
            expanded = showTopMenu,
            onDismissRequest = { showTopMenu = false },
            modifier = Modifier.background(WarmWhiteCard)
          ) {
            DropdownMenuItem(
              text = {
                Text(
                  text = "Health Profile",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = DarkCharcoalText
                )
              },
              onClick = {
                showTopMenu = false
                activeScreen = ActiveScreen.PROFILE
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.HealthAndSafety,
                  contentDescription = null,
                  tint = DeepTealPrimary,
                  modifier = Modifier.size(20.dp)
                )
              },
              modifier = Modifier.testTag("menu_item_profile")
            )

            DropdownMenuItem(
              text = {
                Text(
                  text = "Clinical Safety Rules",
                  fontSize = 14.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = DarkCharcoalText
                )
              },
              onClick = {
                showTopMenu = false
                activeScreen = ActiveScreen.SAFETY
              },
              leadingIcon = {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = DeepTealPrimary,
                  modifier = Modifier.size(20.dp)
                )
              },
              modifier = Modifier.testTag("menu_item_safety")
            )
          }
        }
      }
    },
    bottomBar = {
      // Bottom navigation only displays primary daily tracking tabs: HOME, PLANNER, HISTORY
      NavigationBar(
        containerColor = WarmWhiteCard,
        contentColor = DarkCharcoalText,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier.testTag("nutripath_bottom_navigation")
      ) {
        NutriPathTab.values().forEach { tab ->
          val isSelected = (activeScreen == ActiveScreen.MAIN_TAB && currentTab == tab)
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
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (activeScreen) {
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
            onLaunchOnboarding = {
              activeScreen = ActiveScreen.MAIN_TAB
              viewModel.restartOnboarding()
            }
          )
        }
        ActiveScreen.SAFETY -> {
          SettingsSafetyScreen(
            viewModel = viewModel,
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
                }
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
