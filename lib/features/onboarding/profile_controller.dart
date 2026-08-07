import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../../core/providers.dart';
import 'models.dart';

/// Current demo profile. `null` = not onboarded yet (or reset).
final profileControllerProvider =
    AsyncNotifierProvider<ProfileController, DemoProfile?>(ProfileController.new);

/// Available conditions / dietary restrictions for the selection screens.
final conditionsProvider = FutureProvider<List<Condition>>((ref) async {
  final rows = await ref.watch(supabaseClientProvider).from('conditions').select('*').order('name');
  return (rows as List).map((m) => Condition.fromMap(m as Map<String, dynamic>)).toList();
});

final restrictionsProvider = FutureProvider<List<DietaryRestriction>>((ref) async {
  final rows = await ref.watch(supabaseClientProvider).from('dietary_restrictions').select('*').order('name');
  return (rows as List)
      .map((m) => DietaryRestriction.fromMap(m as Map<String, dynamic>))
      .toList();
});

class ProfileController extends AsyncNotifier<DemoProfile?> {
  static const _prefsKey = 'demo_profile_id';

  @override
  Future<DemoProfile?> build() async {
    final prefs = await SharedPreferences.getInstance();
    final id = prefs.getString(_prefsKey);
    if (id == null) return null;

    final client = ref.read(supabaseClientProvider);
    final profileRows = await client.from('profiles').select('*').eq('id', id).limit(1);
    if (profileRows.isEmpty) return null;

    final profile = DemoProfile.fromMap(profileRows.first);
    final condRows = await client
        .from('profile_conditions')
        .select('condition_id')
        .eq('profile_id', id);
    final restrRows = await client
        .from('profile_restrictions')
        .select('restriction_id')
        .eq('profile_id', id);

    return DemoProfile(
      id: profile.id,
      displayName: profile.displayName,
      age: profile.age,
      sex: profile.sex,
      heightCm: profile.heightCm,
      weightKg: profile.weightKg,
      activityLevel: profile.activityLevel,
      conditionIds: [
        for (final m in condRows as List) (m as Map<String, dynamic>)['condition_id'] as String,
      ],
      restrictionIds: [
        for (final m in restrRows as List) (m as Map<String, dynamic>)['restriction_id'] as String,
      ],
    );
  }

  /// Persist the assembled demo profile (upsert + replace selections).
  Future<void> saveProfile(DemoProfile draft) async {
    final client = ref.read(supabaseClientProvider);

    await client.from('profiles').upsert(draft.toRow(), onConflict: 'id');

    await client.from('profile_conditions').delete().eq('profile_id', draft.id);
    if (draft.conditionIds.isNotEmpty) {
      await client.from('profile_conditions').insert([
        for (final cid in draft.conditionIds)
          {'profile_id': draft.id, 'condition_id': cid},
      ]);
    }

    await client.from('profile_restrictions').delete().eq('profile_id', draft.id);
    if (draft.restrictionIds.isNotEmpty) {
      await client.from('profile_restrictions').insert([
        for (final rid in draft.restrictionIds)
          {'profile_id': draft.id, 'restriction_id': rid},
      ]);
    }

    state = AsyncData(draft);
  }

  /// Persist the assembled demo profile (upsert + replace selections) and mark
  /// onboarding complete.
  Future<void> completeOnboarding(DemoProfile draft) async {
    final prefs = await SharedPreferences.getInstance();
    await saveProfile(draft);
    await prefs.setString(_prefsKey, draft.id);
  }

  /// Clear demo data and return to onboarding.
  Future<void> reset() async {
    final prefs = await SharedPreferences.getInstance();
    final id = prefs.getString(_prefsKey);
    final client = ref.read(supabaseClientProvider);
    if (id != null) {
      await client.from('profiles').delete().eq('id', id);
    }
    await prefs.remove(_prefsKey);
    state = const AsyncData(null);
  }
}
