/// Domain models for onboarding + the demo profile.
library;

import 'package:flutter/material.dart';

class Condition {
  const Condition({required this.id, required this.slug, required this.name, this.description});
  final String id;
  final String slug;
  final String name;
  final String? description;

  factory Condition.fromMap(Map<String, dynamic> m) => Condition(
        id: m['id'] as String,
        slug: m['slug'] as String,
        name: m['name'] as String,
        description: m['description'] as String?,
      );

  static const conditionIcons = <String, IconData>{
    'type2_diabetes': Icons.water_drop_outlined,
    'hypertension': Icons.favorite_border,
    'dyslipidemia': Icons.monitor_heart_outlined,
    'ckd': Icons.health_and_safety_outlined,
    'gout': Icons.healing_outlined,
    'arthritis': Icons.accessibility_new_outlined,
    'hypothyroidism': Icons.all_inclusive_outlined,
    'nafld': Icons.energy_savings_leaf_outlined,
    'obesity': Icons.monitor_weight_outlined,
    'celiac': Icons.no_meals_outlined,
  };

  IconData get icon => conditionIcons[slug] ?? Icons.spa_outlined;
}

class DietaryRestriction {
  const DietaryRestriction({required this.id, required this.slug, required this.name, this.description});
  final String id;
  final String slug;
  final String name;
  final String? description;

  factory DietaryRestriction.fromMap(Map<String, dynamic> m) => DietaryRestriction(
        id: m['id'] as String,
        slug: m['slug'] as String,
        name: m['name'] as String,
        description: m['description'] as String?,
      );
}

/// The demo-mode profile assembled from onboarding (mirrors the `profiles` row
/// plus selections).
class DemoProfile {
  const DemoProfile({
    required this.id,
    this.displayName,
    this.age,
    this.sex,
    this.heightCm,
    this.weightKg,
    this.activityLevel,
    this.conditionIds = const [],
    this.restrictionIds = const [],
  });

  final String id;
  final String? displayName;
  final int? age;
  final String? sex;
  final double? heightCm;
  final double? weightKg;
  final String? activityLevel;
  final List<String> conditionIds;
  final List<String> restrictionIds;

  bool get isComplete =>
      displayName != null &&
      age != null &&
      sex != null &&
      heightCm != null &&
      weightKg != null &&
      activityLevel != null;

  DemoProfile copyWith({
    String? displayName,
    int? age,
    String? sex,
    double? heightCm,
    double? weightKg,
    String? activityLevel,
    List<String>? conditionIds,
    List<String>? restrictionIds,
  }) =>
      DemoProfile(
        id: id,
        displayName: displayName ?? this.displayName,
        age: age ?? this.age,
        sex: sex ?? this.sex,
        heightCm: heightCm ?? this.heightCm,
        weightKg: weightKg ?? this.weightKg,
        activityLevel: activityLevel ?? this.activityLevel,
        conditionIds: conditionIds ?? this.conditionIds,
        restrictionIds: restrictionIds ?? this.restrictionIds,
      );

  Map<String, dynamic> toRow() => {
        'id': id,
        'display_name': displayName,
        'age': age,
        'sex': sex,
        'height_cm': heightCm,
        'weight_kg': weightKg,
        'activity_level': activityLevel,
      };

  factory DemoProfile.fromMap(Map<String, dynamic> m) => DemoProfile(
        id: m['id'] as String,
        displayName: m['display_name'] as String?,
        age: m['age'] as int?,
        sex: m['sex'] as String?,
        heightCm: (m['height_cm'] as num?)?.toDouble(),
        weightKg: (m['weight_kg'] as num?)?.toDouble(),
        activityLevel: m['activity_level'] as String?,
      );
}
