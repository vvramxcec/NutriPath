/// Client-side shape validation for the meal-plan JSON the edge function
/// produces (mirrors `_shared/validation.ts` in the edge function). Kept pure
/// Dart so it can be unit-tested without Flutter, and documents the data
/// contract the app expects.
library;

const mealTypes = ['breakfast', 'lunch', 'dinner', 'snack'];

/// Validated plan shape — matches what `MealPlan.fromMap` can render.
class ValidatedPlan {
  const ValidatedPlan({
    required this.summary,
    required this.days,
    required this.itemCount,
  });
  final String summary;
  final List<Map<String, dynamic>> days;
  final int itemCount;
}

class PlanValidationError implements Exception {
  const PlanValidationError(this.message);
  final String message;
  @override
  String toString() => 'PlanValidationError: $message';
}

bool _isRecord(dynamic v) => v is Map && v is! List;

/// Strict shape check of a raw plan payload. Throws [PlanValidationError] on
/// any structural issue, otherwise returns a normalized view.
ValidatedPlan validatePlan(dynamic data) {
  if (!_isRecord(data)) throw const PlanValidationError('plan root must be an object');
  final summary = data['summary'];
  if (summary is! String) throw const PlanValidationError('plan.summary must be a string');
  final days = data['days'];
  if (days is! List || days.isEmpty) {
    throw const PlanValidationError('plan.days must be a non-empty array');
  }

  var itemCount = 0;
  final normalizedDays = <Map<String, dynamic>>[];
  for (var di = 0; di < days.length; di++) {
    final d = days[di];
    if (!_isRecord(d)) throw PlanValidationError('days[$di] must be an object');
    final day = d['day'];
    if (day is! num || day != day.roundToDouble()) {
      throw PlanValidationError('days[$di].day must be an integer');
    }
    final meals = d['meals'];
    if (meals is! List || meals.isEmpty) {
      throw PlanValidationError('days[$di].meals must be a non-empty array');
    }

    final normalizedMeals = <Map<String, dynamic>>[];
    for (var mi = 0; mi < meals.length; mi++) {
      final m = meals[mi];
      if (!_isRecord(m)) throw PlanValidationError('days[$di].meals[$mi] must be an object');
      final mealType = m['meal_type'];
      if (mealType is! String || !mealTypes.contains(mealType)) {
        throw PlanValidationError('days[$di].meals[$mi].meal_type invalid');
      }
      final name = m['name'];
      if (name is! String) throw PlanValidationError('days[$di].meals[$mi].name must be a string');
      final items = m['items'];
      if (items is! List || items.isEmpty) {
        throw PlanValidationError('days[$di].meals[$mi].items must be a non-empty array');
      }

      final normalizedItems = <Map<String, dynamic>>[];
      for (final it in items) {
        if (!_isRecord(it)) throw const PlanValidationError('plan item must be an object');
        final foodName = it['food_name'];
        if (foodName is! String || foodName.trim().isEmpty) {
          throw const PlanValidationError('plan item food_name must be a non-empty string');
        }
        normalizedItems.add({
          'food_name': foodName,
          'portion': it['portion'] is String ? it['portion'] : null,
          'serving_count': it['serving_count'] is num ? it['serving_count'] : 1,
          'notes': it['notes'] is String ? it['notes'] : null,
        });
        itemCount++;
      }

      normalizedMeals.add({
        'meal_type': mealType,
        'name': name,
        'items': normalizedItems,
      });
    }

    normalizedDays.add({
      'day': day.toInt(),
      'meals': normalizedMeals,
      'total_calories_kcal': d['total_calories_kcal'] is num ? d['total_calories_kcal'] : 0,
      'protein_g': d['protein_g'] is num ? d['protein_g'] : null,
      'carbs_g': d['carbs_g'] is num ? d['carbs_g'] : null,
      'fat_g': d['fat_g'] is num ? d['fat_g'] : null,
    });
  }

  return ValidatedPlan(summary: summary, days: normalizedDays, itemCount: itemCount);
}
