import 'package:flutter_test/flutter_test.dart';
import 'package:nutripath/features/meal_plan/plan_validation.dart';

void main() {
  group('validatePlan', () {
    Map<String, dynamic> validPlan() => {
          'summary': 'A 3-day plan for your goals.',
          'days': [
            {
              'day': 1,
              'meals': [
                {
                  'meal_type': 'breakfast',
                  'name': 'Breakfast',
                  'items': [
                    {'food_name': 'Masala Oats', 'portion': '1 bowl', 'serving_count': 1},
                    {'food_name': 'Black Tea', 'notes': 'No sugar'},
                  ],
                },
                {
                  'meal_type': 'dinner',
                  'name': 'Dinner',
                  'items': [
                    {'food_name': 'Dal Khichdi', 'serving_count': 1.5},
                  ],
                },
              ],
              'total_calories_kcal': 1650,
              'protein_g': 72,
              'carbs_g': 210,
              'fat_g': 48,
            },
            {
              'day': 2,
              'meals': [
                {
                  'meal_type': 'lunch',
                  'name': 'Lunch',
                  'items': [
                    {'food_name': 'Vegetable Pulao'},
                  ],
                },
              ],
              'total_calories_kcal': 1580,
            },
          ],
        };

    test('accepts a well-formed plan', () {
      final result = validatePlan(validPlan());
      expect(result.summary, startsWith('A 3-day plan'));
      expect(result.days, hasLength(2));
      expect(result.itemCount, 4);
      // first meal items normalize serving_count defaults
      final meals = result.days[0]['meals'] as List;
      final breakfast = meals[0] as Map<String, dynamic>;
      final items = breakfast['items'] as List;
      expect((items[1] as Map<String, dynamic>)['serving_count'], 1);
      expect((items[1] as Map<String, dynamic>)['notes'], 'No sugar');
    });

    test('rejects a non-object root', () {
      expect(() => validatePlan(null), throwsA(isA<PlanValidationError>()));
      expect(() => validatePlan([1, 2, 3]), throwsA(isA<PlanValidationError>()));
    });

    test('rejects missing summary', () {
      final plan = validPlan()..remove('summary');
      expect(() => validatePlan(plan), throwsA(isA<PlanValidationError>()));
    });

    test('rejects empty days', () {
      final plan = validPlan()..['days'] = <Object>[];
      expect(() => validatePlan(plan), throwsA(isA<PlanValidationError>()));
    });

    test('rejects a non-integer day', () {
      final plan = validPlan();
      (plan['days'] as List)[0] = {'day': 1.5, 'meals': [], 'total_calories_kcal': 0};
      expect(() => validatePlan(plan), throwsA(isA<PlanValidationError>()));
    });

    test('rejects an unknown meal_type', () {
      final plan = validPlan();
      final meals = ((plan['days'] as List)[0] as Map<String, dynamic>)['meals'] as List;
      (meals[0] as Map<String, dynamic>)['meal_type'] = 'midnight';
      expect(() => validatePlan(plan), throwsA(isA<PlanValidationError>()));
    });

    test('rejects a meal with no items', () {
      final plan = validPlan();
      final meals = ((plan['days'] as List)[0] as Map<String, dynamic>)['meals'] as List;
      (meals[1] as Map<String, dynamic>)['items'] = <Object>[];
      expect(() => validatePlan(plan), throwsA(isA<PlanValidationError>()));
    });

    test('rejects an item with an empty food_name', () {
      final plan = validPlan();
      final items = (((plan['days'] as List)[0] as Map<String, dynamic>)['meals']
              as List)[0]['items'] as List;
      items.add({'food_name': '   '});
      expect(() => validatePlan(plan), throwsA(isA<PlanValidationError>()));
    });

    test('normalizes optional numeric fields', () {
      final plan = validPlan();
      final day = (plan['days'] as List)[1] as Map<String, dynamic>;
      day['protein_g'] = 'nope'; // non-numeric → null, not an error
      final result = validatePlan(plan);
      expect(result.days[1]['protein_g'], isNull);
      expect(result.days[1]['total_calories_kcal'], 1580);
    });
  });
}
