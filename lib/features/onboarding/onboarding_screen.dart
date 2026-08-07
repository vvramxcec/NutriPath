import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:uuid/uuid.dart';

import '../../core/theme/app_theme.dart';
import '../../shared/widgets/primary_button.dart';
import 'models.dart';
import 'profile_controller.dart';
import 'profile_setup_step.dart';
import 'condition_selection_step.dart';
import 'restriction_selection_step.dart';

/// Three-step onboarding (profile → conditions → restrictions) with a thin
/// gold progress bar, finishing by persisting the demo profile.
class OnboardingScreen extends ConsumerStatefulWidget {
  const OnboardingScreen({super.key});

  @override
  ConsumerState<OnboardingScreen> createState() => _OnboardingScreenState();
}

class _OnboardingScreenState extends ConsumerState<OnboardingScreen> {
  int _step = 0;
  DemoProfile? _draft;
  final _profileFormKey = GlobalKey<FormState>();

  static const _steps = ['Your profile', 'Health conditions', 'Dietary preferences'];

  Future<void> _next() async {
    if (_step == 0) {
      final ok = _profileFormKey.currentState?.validate() ?? false;
      if (!ok || _draft == null || !_draft!.isComplete) return;
    }
    if (_step == 1 && (_draft?.conditionIds.isEmpty ?? true)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Select at least one condition.')),
      );
      return;
    }
    if (_step < 2) {
      setState(() => _step++);
      return;
    }
    await ref.read(profileControllerProvider.notifier).completeOnboarding(_draft!);
    if (!mounted) return;
    context.go('/dashboard');
  }

  @override
  Widget build(BuildContext context) {
    final draft = _draft;
    return Scaffold(
      backgroundColor: AppColors.cream,
      body: SafeArea(
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 16, 20, 8),
              child: Row(
                children: [
                  Text(
                    'NutriPath',
                    style: Theme.of(context)
                        .textTheme
                        .titleMedium
                        ?.copyWith(fontWeight: FontWeight.w700, color: AppColors.deepAmber),
                  ),
                  const Spacer(),
                  Text(
                    '${_step + 1} / ${_steps.length}',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
                  ),
                ],
              ),
            ),
            _ProgressBar(step: _step, steps: _steps),
            Expanded(
              child: AnimatedSwitcher(
                duration: const Duration(milliseconds: 250),
                child: switch (_step) {
                  0 => ProfileSetupStep(
                      key: ValueKey(0),
                      formKey: _profileFormKey,
                      initial: draft,
                      onChanged: (d) => setState(() => _draft = d),
                    ),
                  1 => ConditionSelectionStep(
                      key: ValueKey(1),
                      selectedIds: draft?.conditionIds ?? const [],
                      onChanged: (ids) => setState(
                        () => _draft = (draft ?? DemoProfile(id: _newProfileId())).copyWith(
                          conditionIds: ids,
                        ),
                      ),
                    ),
                  _ => RestrictionSelectionStep(
                      key: ValueKey(2),
                      selectedIds: draft?.restrictionIds ?? const [],
                      onChanged: (ids) => setState(
                        () => _draft = (draft ?? DemoProfile(id: _newProfileId())).copyWith(
                          restrictionIds: ids,
                        ),
                      ),
                    ),
                },
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
              child: PrimaryButton(
                label: _step < 2 ? 'Continue' : 'Create my plan',
                icon: _step < 2 ? Icons.arrow_forward : Icons.auto_awesome,
                onPressed: _next,
              ),
            ),
          ],
        ),
      ),
    );
  }

  static const _uuid = Uuid();

  String _newProfileId() => _uuid.v4();
}

class _ProgressBar extends StatelessWidget {
  const _ProgressBar({required this.step, required this.steps});

  final int step;
  final List<String> steps;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      child: Row(
        children: [
          for (var i = 0; i < steps.length; i++)
            Expanded(
              child: Container(
                height: 3,
                margin: EdgeInsets.only(right: i < steps.length - 1 ? 6 : 0),
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(2),
                  color: i <= step ? AppColors.deepAmber : AppColors.line,
                ),
              ),
            ),
        ],
      ),
    );
  }
}
