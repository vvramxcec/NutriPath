package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = DeepTealPrimary,
  onPrimary = Color.White,
  primaryContainer = DeepTealContainer,
  onPrimaryContainer = DeepTealOnContainer,
  secondary = DeepTealDark,
  onSecondary = Color.White,
  background = LimeYellowBackground,
  onBackground = DarkCharcoalText,
  surface = WarmWhiteCard,
  onSurface = DarkCharcoalText,
  surfaceVariant = WarmWhiteCardSubtle,
  onSurfaceVariant = TextMuted,
  outline = BorderSubtle,
  error = StatusRejectedRed,
  onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
  primary = LimeYellowAccent,
  onPrimary = DeepTealDark,
  primaryContainer = DeepTealPrimary,
  onPrimaryContainer = Color.White,
  secondary = DeepTealContainer,
  onSecondary = DeepTealDark,
  background = Color(0xFF141916),
  onBackground = Color(0xFFE8ECE8),
  surface = Color(0xFF1E2421),
  onSurface = Color(0xFFE8ECE8),
  surfaceVariant = Color(0xFF28302C),
  onSurfaceVariant = Color(0xFFB5BFB9),
  outline = Color(0xFF38433E),
  error = Color(0xFFF87171),
  onError = Color.Black
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent NutriPath brand lime-teal palette
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
