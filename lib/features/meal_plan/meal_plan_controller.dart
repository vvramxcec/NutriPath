import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/providers.dart';
import '../onboarding/profile_controller.dart';
import 'meal_plan_repository.dart';
import 'models.dart';

final mealPlanRepositoryProvider =
    Provider<MealPlanRepository>((ref) => MealPlanRepository(ref.watch(supabaseClientProvider)));

/// The latest plan for the current profile (dashboard "today's plan").
final latestPlanProvider = FutureProvider.autoDispose<MealPlan?>((ref) async {
  final profile = ref.watch(profileControllerProvider).valueOrNull;
  if (profile == null) return null;
  return ref.watch(mealPlanRepositoryProvider).latestPlan(profile.id);
});

/// A single plan by id (detail screen).
final planByIdProvider =
    FutureProvider.autoDispose.family<MealPlan, String>((ref, planId) {
  return ref.watch(mealPlanRepositoryProvider).fetchPlan(planId);
});

/// Recent plans for the history screen.
final plansHistoryProvider = FutureProvider.autoDispose<List<MealPlan>>((ref) async {
  final profile = ref.watch(profileControllerProvider).valueOrNull;
  if (profile == null) return const [];
  return ref.watch(mealPlanRepositoryProvider).fetchPlans(profile.id);
});

/// Drives the generation screen (staged loading → success/error → detail).
final generationControllerProvider =
    AsyncNotifierProvider<GenerationController, MealPlan?>(GenerationController.new);

class GenerationController extends AsyncNotifier<MealPlan?> {
  @override
  Future<MealPlan?> build() async => null;

  Future<MealPlan?> generate({int days = 3}) async {
    final profile = ref.read(profileControllerProvider).valueOrNull;
    if (profile == null) throw StateError('No profile — onboard first.');

    state = const AsyncLoading();
    try {
      final plan = await ref
          .read(mealPlanRepositoryProvider)
          .generate(profileId: profile.id, days: days);
      state = AsyncData(plan);
      ref.invalidate(latestPlanProvider);
      return plan;
    } catch (e) {
      state = AsyncError(e, StackTrace.current);
      rethrow;
    }
  }
}
