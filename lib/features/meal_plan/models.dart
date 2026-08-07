/// Meal-plan domain models (mirror the Supabase rows).
library;

import 'package:flutter/material.dart';

class MealPlanItem {
  const MealPlanItem({
    required this.id,
    required this.day,
    required this.mealType,
    required this.foodName,
    this.portion,
    this.servingCount = 1,
    this.notes,
    this.foodItemId,
    this.kcalPerServing,
    this.proteinPerServing,
    this.carbsPerServing,
    this.fatPerServing,
  });

  final String id;
  final int day;
  final String mealType;
  final String foodName;
  final String? portion;
  final double servingCount;
  final String? notes;
  final String? foodItemId;
  final double? kcalPerServing;
  final double? proteinPerServing;
  final double? carbsPerServing;
  final double? fatPerServing;

  double get kcal => (kcalPerServing ?? 0) * servingCount;
  double get protein => (proteinPerServing ?? 0) * servingCount;
  double get carbs => (carbsPerServing ?? 0) * servingCount;
  double get fat => (fatPerServing ?? 0) * servingCount;

  factory MealPlanItem.fromMap(Map<String, dynamic> m) => MealPlanItem(
        id: m['id'] as String,
        day: m['day'] as int,
        mealType: m['meal_type'] as String,
        foodName: m['food_name'] as String,
        portion: m['portion'] as String?,
        servingCount: ((m['serving_count'] as num?) ?? 1).toDouble(),
        notes: m['notes'] as String?,
        foodItemId: m['food_item_id'] as String?,
        kcalPerServing: (m['kcal_per_serving'] as num?)?.toDouble(),
        proteinPerServing: (m['protein_g_per_serving'] as num?)?.toDouble(),
        carbsPerServing: (m['carbs_g_per_serving'] as num?)?.toDouble(),
        fatPerServing: (m['fat_g_per_serving'] as num?)?.toDouble(),
      );

  static const mealTypeOrder = ['breakfast', 'lunch', 'dinner', 'snack'];
  static const mealTypeLabels = <String, String>{
    'breakfast': 'Breakfast',
    'lunch': 'Lunch',
    'dinner': 'Dinner',
    'snack': 'Snack',
  };
  static const mealTypeIcons = <String, IconData>{
    'breakfast': Icons.free_breakfast_outlined,
    'lunch': Icons.lunch_dining_outlined,
    'dinner': Icons.nightlight_outlined,
    'snack': Icons.cookie_outlined,
  };

  String get mealLabel => mealTypeLabels[mealType] ?? mealType;
  IconData get mealIcon => mealTypeIcons[mealType] ?? Icons.restaurant_outlined;
}

class MealPlan {
  const MealPlan({
    required this.id,
    required this.profileId,
    this.planDate,
    required this.status,
    this.totalCaloriesKcal,
    this.generatedBy,
    this.modelName,
    this.summary,
    this.errorMessage,
    this.items = const [],
  });

  final String id;
  final String profileId;
  final DateTime? planDate;
  final String status;
  final double? totalCaloriesKcal;
  final String? generatedBy;
  final String? modelName;
  final String? summary;
  final String? errorMessage;
  final List<MealPlanItem> items;

  factory MealPlan.fromMap(Map<String, dynamic> m) => MealPlan(
        id: m['id'] as String,
        profileId: m['profile_id'] as String,
        planDate: m['plan_date'] != null ? DateTime.tryParse(m['plan_date'] as String) : null,
        status: m['status'] as String,
        totalCaloriesKcal: (m['total_calories_kcal'] as num?)?.toDouble(),
        generatedBy: m['generated_by'] as String?,
        modelName: m['model_name'] as String?,
        summary: (m['metadata'] as Map<String, dynamic>?)?['summary'] as String?,
        errorMessage: m['error_message'] as String?,
        items: [
          for (final it in (m['meal_plan_items'] as List? ?? []))
            MealPlanItem.fromMap(it as Map<String, dynamic>),
        ],
      );

  /// Per-day grouping in canonical meal order.
  Map<int, List<MealPlanItem>> get itemsByDay {
    final map = <int, List<MealPlanItem>>{};
    for (final it in items) {
      map.putIfAbsent(it.day, () => []).add(it);
    }
    for (final list in map.values) {
      list.sort((a, b) =>
          MealPlanItem.mealTypeOrder.indexOf(a.mealType) -
          MealPlanItem.mealTypeOrder.indexOf(b.mealType));
    }
    return map;
  }

  int get dayCount => itemsByDay.keys.length;

  double caloriesOnDay(int day) =>
      items.where((it) => it.day == day).fold(0, (s, it) => s + it.kcal);

  double proteinOnDay(int day) =>
      items.where((it) => it.day == day).fold(0, (s, it) => s + it.protein);

  double carbsOnDay(int day) =>
      items.where((it) => it.day == day).fold(0, (s, it) => s + it.carbs);

  double fatOnDay(int day) =>
      items.where((it) => it.day == day).fold(0, (s, it) => s + it.fat);
}
