import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/theme/app_theme.dart';
import '../../shared/widgets/primary_button.dart';
import 'meal_plan_controller.dart';

/// Generates a meal plan through the edge function. Shows staged progress,
/// then routes to the plan detail on success, or a retry on error.
class GenerationScreen extends ConsumerStatefulWidget {
  const GenerationScreen({super.key});

  @override
  ConsumerState<GenerationScreen> createState() => _GenerationScreenState();
}

class _GenerationScreenState extends ConsumerState<GenerationScreen> {
  static const _stages = [
    'Reviewing your profile…',
    'Checking your health limits…',
    'Choosing the right foods…',
    'Building your personalized plan…',
  ];

  bool _started = false;
  int _stageIndex = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) => _start());
  }

  Future<void> _start() async {
    if (_started) return;
    setState(() => _started = true);
    // rotate the staged messages while loading
    Future.delayed(const Duration(seconds: 2), () {
      if (mounted && _stageIndex < _stages.length - 1) {
        setState(() => _stageIndex++);
      }
    });
    try {
      final plan = await ref.read(generationControllerProvider.notifier).generate(days: 3);
      if (plan != null && mounted) context.go('/dashboard/plan/${plan.id}');
    } catch (_) {
      // error surface below via the controller state
    }
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(generationControllerProvider);
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(gradient: AppTheme.gradient),
        child: SafeArea(
          child: Center(
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 32),
              child: state.when(
                loading: () => _LoadingBody(stage: _stages[_stageIndex]),
                error: (e, _) => _ErrorBody(message: e.toString(), onRetry: _start),
                data: (_) => const _LoadingBody(stage: 'Finalizing…'),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _LoadingBody extends StatelessWidget {
  const _LoadingBody({required this.stage});
  final String stage;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const SizedBox(
          width: 56,
          height: 56,
          child: CircularProgressIndicator(color: Colors.white, strokeWidth: 4),
        ),
        const SizedBox(height: 24),
        Text(
          stage,
          textAlign: TextAlign.center,
          style: const TextStyle(color: Colors.white, fontSize: 16, fontWeight: FontWeight.w600),
        ),
      ],
    );
  }
}

class _ErrorBody extends StatelessWidget {
  const _ErrorBody({required this.message, required this.onRetry});
  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        const Icon(Icons.error_outline, color: Colors.white, size: 48),
        const SizedBox(height: 16),
        const Text(
          'We couldn\'t generate your plan',
          style: TextStyle(color: Colors.white, fontSize: 18, fontWeight: FontWeight.w700),
        ),
        const SizedBox(height: 8),
        Text(
          message,
          textAlign: TextAlign.center,
          maxLines: 4,
          overflow: TextOverflow.ellipsis,
          style: const TextStyle(color: Colors.white70, fontSize: 13),
        ),
        const SizedBox(height: 24),
        PrimaryButton(
          label: 'Try again',
          icon: Icons.refresh,
          onPressed: onRetry,
        ),
      ],
    );
  }
}
