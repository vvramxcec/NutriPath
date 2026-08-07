/// NutriPath visual language — warm gold/amber premium wellness aesthetic
/// (mirrors the calorie-tracker reference mockup).
///
/// Palette: deep amber -> soft gold -> cream gradient, off-white body, white
/// cards, black accents. Typography: Inter. Cards: 14px radius, soft shadow.
library;

import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

abstract final class AppColors {
  static const Color deepAmber = Color(0xFFC49A2C);
  static const Color softGold = Color(0xFFE8D48B);
  static const Color cream = Color(0xFFFDF8EC);
  static const Color body = Color(0xFFF8F7F4);
  static const Color card = Colors.white;
  static const Color ink = Color(0xFF1A1A1A);
  static const Color inkSoft = Color(0xFF3A3A3A);
  static const Color muted = Color(0xFF9E9E9E);
  static const Color line = Color(0xFFEDEAE2);
  static const Color danger = Color(0xFFC0392B);
}

abstract final class AppTheme {
  static const BorderRadius cardRadius = BorderRadius.all(Radius.circular(14));
  static const List<BoxShadow> cardShadow = [
    BoxShadow(color: Color(0x0F000000), blurRadius: 8, offset: Offset(0, 2)),
  ];

  /// Warm gradient for the dashboard header / hero surfaces.
  static const LinearGradient gradient = LinearGradient(
    begin: Alignment.topCenter,
    end: Alignment.bottomCenter,
    colors: [AppColors.deepAmber, AppColors.softGold, AppColors.cream],
  );

  static ThemeData light() {
    final base = ThemeData.light(useMaterial3: true);
    final textTheme = GoogleFonts.interTextTheme(base.textTheme);
    return base.copyWith(
      scaffoldBackgroundColor: AppColors.body,
      colorScheme: ColorScheme.fromSeed(
        seedColor: AppColors.deepAmber,
        primary: AppColors.deepAmber,
        onPrimary: Colors.white,
        secondary: AppColors.softGold,
        surface: AppColors.body,
        onSurface: AppColors.ink,
      ),
      textTheme: textTheme,
      appBarTheme: AppBarTheme(
        backgroundColor: Colors.transparent,
        elevation: 0,
        centerTitle: false,
        titleTextStyle: textTheme.headlineSmall?.copyWith(
          fontWeight: FontWeight.w600,
          color: AppColors.ink,
        ),
      ),
      cardTheme: CardThemeData(
        color: AppColors.card,
        elevation: 0,
        shape: RoundedRectangleBorder(borderRadius: cardRadius),
        margin: EdgeInsets.zero,
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.deepAmber,
          foregroundColor: Colors.white,
          minimumSize: const Size.fromHeight(52),
          shape: RoundedRectangleBorder(borderRadius: cardRadius),
          elevation: 0,
          textStyle: textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: AppColors.deepAmber,
          side: const BorderSide(color: AppColors.deepAmber),
          shape: RoundedRectangleBorder(borderRadius: cardRadius),
          minimumSize: const Size.fromHeight(48),
          textStyle: textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600),
        ),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: AppColors.ink,
        foregroundColor: Colors.white,
        elevation: 6,
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: AppColors.card,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        border: OutlineInputBorder(
          borderRadius: cardRadius,
          borderSide: const BorderSide(color: AppColors.line),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: cardRadius,
          borderSide: const BorderSide(color: AppColors.line),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: cardRadius,
          borderSide: const BorderSide(color: AppColors.deepAmber, width: 1.5),
        ),
      ),
      dividerTheme: const DividerThemeData(color: AppColors.line, thickness: 1),
      snackBarTheme: SnackBarThemeData(
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: cardRadius),
      ),
    );
  }
}

/// The big white-on-gradient hero number (dashboard calories).
abstract final class HeroText {
  static TextStyle number(BuildContext context) => GoogleFonts.inter(
        fontSize: 64,
        fontWeight: FontWeight.w700,
        color: Colors.white,
        height: 1.0,
      );
}
