import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:uuid/uuid.dart';

import 'models.dart';

/// Data access for meal plans (read + generate via the edge function).
class MealPlanRepository {
  MealPlanRepository(this._client);
  final SupabaseClient _client;
  static const _uuid = Uuid();

  /// Create the optimistic `pending` plan row the edge function will fill in.
  Future<MealPlan> createPending(String profileId) async {
    final rows = await _client
        .from('meal_plans')
        .insert({'id': _uuid.v4(), 'profile_id': profileId, 'status': 'pending'})
        .select();
    return MealPlan.fromMap(rows.first);
  }

  /// Most recent plan (regardless of status) — "today's plan" for the dashboard.
  Future<MealPlan?> latestPlan(String profileId) async {
    final rows = await _client
        .from('meal_plans')
        .select('*, meal_plan_items(*)')
        .eq('profile_id', profileId)
        .order('created_at', ascending: false)
        .limit(1);
    if (rows.isEmpty) return null;
    return MealPlan.fromMap(rows.first);
  }

  Future<MealPlan> fetchPlan(String planId) async {
    final rows = await _client
        .from('meal_plans')
        .select('*, meal_plan_items(*)')
        .eq('id', planId)
        .limit(1);
    if (rows.isEmpty) throw StateError('Plan not found');
    return MealPlan.fromMap(rows.first);
  }

  /// Recent plans for the history screen.
  Future<List<MealPlan>> fetchPlans(String profileId) async {
    final rows = await _client
        .from('meal_plans')
        .select('*, meal_plan_items(*)')
        .eq('profile_id', profileId)
        .order('created_at', ascending: false)
        .limit(20);
    return [for (final r in rows) MealPlan.fromMap(r)];
  }

  /// Generate a plan: ensure a pending row, invoke the edge function, then
  /// refetch the persisted (success) plan. Throws on generation failure — the
  /// edge function records the error on the plan row.
  Future<MealPlan> generate({
    required String profileId,
    String? planId,
    int days = 3,
  }) async {
    final pendingId = planId ?? (await createPending(profileId)).id;
    await _client.functions.invoke('generate-meal-plan', body: {
      'profileId': profileId,
      'planId': pendingId,
      'days': days,
    });
    return fetchPlan(pendingId);
  }
}
