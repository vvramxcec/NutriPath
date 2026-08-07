/// App-wide configuration from `--dart-define` + calorie math helpers.
///
/// Provide at build/run time:
///   flutter run --dart-define=VITE_SUPABASE_URL=... --dart-define=VITE_SUPABASE_ANON_KEY=...
library;

class AppConfig {
  AppConfig._();

  static const String supabaseUrl = String.fromEnvironment(
    'VITE_SUPABASE_URL',
    defaultValue: '',
  );
  static const String supabaseAnonKey = String.fromEnvironment(
    'VITE_SUPABASE_ANON_KEY',
    defaultValue: '',
  );

  static bool get isConfigured => supabaseUrl.isNotEmpty && supabaseAnonKey.isNotEmpty;
}

/// Mifflin-St Jeor basal metabolic rate.
double? computeBmr({
  double? weightKg,
  double? heightCm,
  int? age,
  String? sex,
}) {
  if (weightKg == null || heightCm == null || age == null) return null;
  final base = 10 * weightKg + 6.25 * heightCm - 5 * age;
  switch (sex) {
    case 'male':
      return base + 5;
    case 'female':
      return base - 161;
    default:
      return base - 78;
  }
}

double activityFactor(String? level) {
  switch (level) {
    case 'light':
      return 1.375;
    case 'moderate':
      return 1.55;
    case 'active':
      return 1.725;
    default:
      return 1.2;
  }
}

/// Total daily energy expenditure; applies a 500 kcal deficit for weight management.
int calorieTarget({
  double? weightKg,
  double? heightCm,
  int? age,
  String? sex,
  String? activityLevel,
  bool weightManagement = false,
}) {
  final bmr = computeBmr(weightKg: weightKg, heightCm: heightCm, age: age, sex: sex);
  if (bmr == null) return 2000;
  final tdee = bmr * activityFactor(activityLevel);
  return (weightManagement ? tdee - 500 : tdee).round();
}

double? computeBmi({double? weightKg, double? heightCm}) {
  if (weightKg == null || heightCm == null || heightCm <= 0) return null;
  final m = heightCm / 100;
  return weightKg / (m * m);
}
