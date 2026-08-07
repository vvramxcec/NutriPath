import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/config/app_config.dart';
import '../../core/theme/app_theme.dart';
import '../../shared/widgets/app_bottom_nav.dart';
import '../../shared/widgets/condition_chip.dart';
import '../../shared/widgets/medical_disclaimer_banner.dart';
import '../onboarding/models.dart';
import '../onboarding/profile_controller.dart';

/// View / edit the demo profile: body stats, togglable health conditions and
/// dietary restrictions (persisted immediately), plus a demo-data reset.
class ProfileScreen extends ConsumerStatefulWidget {
  const ProfileScreen({super.key});

  @override
  ConsumerState<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends ConsumerState<ProfileScreen> {
  Future<void> _persist(DemoProfile next) async {
    try {
      await ref.read(profileControllerProvider.notifier).saveProfile(next);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Could not save changes.\n$e')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final profile = ref.watch(profileControllerProvider).valueOrNull;
    final conditionsAsync = ref.watch(conditionsProvider);
    final restrictionsAsync = ref.watch(restrictionsProvider);

    return Scaffold(
      backgroundColor: AppColors.body,
      appBar: AppBar(title: const Text('Profile')),
      body: Column(
        children: [
          Expanded(
            child: profile == null
                ? const Center(child: CircularProgressIndicator())
                : ListView(
                    padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
                    children: [
                      _SummaryCard(profile: profile),
                      const SizedBox(height: 20),
                      _SectionTitle('Health conditions'),
                      const SizedBox(height: 10),
                      conditionsAsync.when(
                        loading: () => const _SectionLoading(),
                        error: (e, _) => Text('Could not load conditions.\n$e'),
                        data: (conditions) => Column(
                          children: [
                            for (final c in conditions)
                              Padding(
                                padding: const EdgeInsets.only(bottom: 10),
                                child: ConditionChip(
                                  title: c.name,
                                  description: c.description ?? 'Select if this applies to you.',
                                  icon: c.icon,
                                  selected: profile.conditionIds.contains(c.id),
                                  onTap: () => _toggleCondition(profile, conditions, c),
                                ),
                              ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 20),
                      _SectionTitle('Dietary restrictions'),
                      const SizedBox(height: 10),
                      restrictionsAsync.when(
                        loading: () => const _SectionLoading(),
                        error: (e, _) => Text('Could not load restrictions.\n$e'),
                        data: (restrictions) => Column(
                          children: [
                            for (final r in restrictions)
                              Padding(
                                padding: const EdgeInsets.only(bottom: 10),
                                child: ConditionChip(
                                  title: r.name,
                                  description: r.description ?? 'Select if this applies to you.',
                                  icon: Icons.no_food_outlined,
                                  selected: profile.restrictionIds.contains(r.id),
                                  onTap: () => _toggleRestriction(profile, restrictions, r),
                                ),
                              ),
                          ],
                        ),
                      ),
                      const SizedBox(height: 24),
                      _ResetSection(profile: profile),
                    ],
                  ),
          ),
          const MedicalDisclaimerBanner(),
          const AppBottomNav(current: 2),
        ],
      ),
    );
  }

  void _toggleCondition(DemoProfile profile, List<Condition> all, Condition c) {
    final ids = [c.id];
    final selected = profile.conditionIds.contains(c.id);
    // restrictions toggle goes through the same ids array (ignored by toggle)
    final next = selected
        ? profile.conditionIds.where((id) => !ids.contains(id)).toList()
        : [...profile.conditionIds, ...ids];
    _persist(profile.copyWith(conditionIds: next));
  }

  void _toggleRestriction(DemoProfile profile, List<DietaryRestriction> all, DietaryRestriction r) {
    final ids = [r.id];
    final selected = profile.restrictionIds.contains(r.id);
    final next = selected
        ? profile.restrictionIds.where((id) => !ids.contains(id)).toList()
        : [...profile.restrictionIds, ...ids];
    _persist(profile.copyWith(restrictionIds: next));
  }
}

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({required this.profile});
  final DemoProfile profile;

  @override
  Widget build(BuildContext context) {
    final bmi = computeBmi(weightKg: profile.weightKg, heightCm: profile.heightCm);
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: AppTheme.cardRadius,
        boxShadow: AppTheme.cardShadow,
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              gradient: AppTheme.gradient,
              borderRadius: BorderRadius.circular(14),
            ),
            child: const Icon(Icons.person, color: Colors.white, size: 26),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  profile.displayName ?? 'Guest',
                  style: Theme.of(context)
                      .textTheme
                      .titleMedium
                      ?.copyWith(fontWeight: FontWeight.w700, color: AppColors.ink),
                ),
                const SizedBox(height: 2),
                Text(
                  _bodyLine(profile),
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
                ),
                const SizedBox(height: 6),
                Text(
                  bmi != null ? 'BMI ${bmi.toStringAsFixed(1)}' : 'BMI —',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                        color: AppColors.deepAmber,
                      ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _bodyLine(DemoProfile p) {
    final parts = <String>[
      if (p.sex != null) p.sex!,
      if (p.age != null) '${p.age} yrs',
      if (p.heightCm != null) '${p.heightCm!.round()} cm',
      if (p.weightKg != null) '${p.weightKg!.round()} kg',
      if (p.activityLevel != null) _activityLabel(p.activityLevel!),
    ];
    return parts.isEmpty ? 'No body details yet' : parts.join(' · ');
  }

  String _activityLabel(String level) {
    switch (level) {
      case 'sedentary':
        return 'Sedentary';
      case 'light':
        return 'Lightly active';
      case 'moderate':
        return 'Moderately active';
      case 'active':
        return 'Very active';
      default:
        return level;
    }
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle(this.text);
  final String text;

  @override
  Widget build(BuildContext context) {
    return Text(
      text,
      style: Theme.of(context)
          .textTheme
          .titleMedium
          ?.copyWith(fontWeight: FontWeight.w700, color: AppColors.ink),
    );
  }
}

class _SectionLoading extends StatelessWidget {
  const _SectionLoading();

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.symmetric(vertical: 20),
      child: Center(child: CircularProgressIndicator()),
    );
  }
}

class _ResetSection extends ConsumerWidget {
  const _ResetSection({required this.profile});
  final DemoProfile profile;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        _SectionTitle('Demo data'),
        const SizedBox(height: 10),
        Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: AppColors.card,
            borderRadius: AppTheme.cardRadius,
            boxShadow: AppTheme.cardShadow,
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'This demo profile stores its data directly in Supabase without an account.',
                style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                child: OutlinedButton.icon(
                  onPressed: () => _confirmReset(context, ref),
                  icon: const Icon(Icons.delete_outline),
                  label: const Text('Reset demo data'),
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Future<void> _confirmReset(BuildContext context, WidgetRef ref) async {
    final ok = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: const Text('Reset demo data?'),
        content: const Text('This clears your profile, conditions and generated plans. You\'ll return to onboarding.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            child: const Text('Reset'),
          ),
        ],
      ),
    );
    if (ok == true) {
      await ref.read(profileControllerProvider.notifier).reset();
    }
  }
}
