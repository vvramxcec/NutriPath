import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/theme/app_theme.dart';
import '../../shared/widgets/app_bottom_nav.dart';
import '../../shared/widgets/medical_disclaimer_banner.dart';
import '../meal_plan/meal_plan_controller.dart';
import '../meal_plan/models.dart';

/// Recent generated plans — tappable rows that open the full plan detail.
class HistoryScreen extends ConsumerWidget {
  const HistoryScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final plansAsync = ref.watch(plansHistoryProvider);
    return Scaffold(
      backgroundColor: AppColors.body,
      appBar: AppBar(title: const Text('Your plans')),
      body: Column(
        children: [
          Expanded(
            child: plansAsync.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(
                child: Padding(
                  padding: const EdgeInsets.all(24),
                  child: Text('Could not load plans.\n$e', textAlign: TextAlign.center),
                ),
              ),
              data: (plans) {
                if (plans.isEmpty) {
                  return const _EmptyHistory();
                }
                return ListView.separated(
                  padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
                  itemCount: plans.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 10),
                  itemBuilder: (context, i) => _PlanRow(plan: plans[i]),
                );
              },
            ),
          ),
          const MedicalDisclaimerBanner(),
          const AppBottomNav(current: 1),
        ],
      ),
    );
  }
}

class _EmptyHistory extends StatelessWidget {
  const _EmptyHistory();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.history, size: 48, color: AppColors.softGold),
            const SizedBox(height: 12),
            Text(
              'No plans yet.\nTap + on the dashboard to generate one.',
              textAlign: TextAlign.center,
              style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: AppColors.muted),
            ),
          ],
        ),
      ),
    );
  }
}

class _PlanRow extends StatelessWidget {
  const _PlanRow({required this.plan});
  final MealPlan plan;

  @override
  Widget build(BuildContext context) {
    final date = plan.planDate;
    final label = date == null
        ? 'Generated plan'
        : '${date.day} ${_month(date.month)} ${date.year}';
    final status = plan.status;
    final failed = status == 'error';

    return GestureDetector(
      onTap: () => context.push('/dashboard/plan/${plan.id}'),
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: AppColors.card,
          borderRadius: AppTheme.cardRadius,
          boxShadow: AppTheme.cardShadow,
        ),
        child: Row(
          children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                color: failed ? AppColors.body : AppColors.softGold.withValues(alpha: 0.4),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(
                failed ? Icons.error_outline : Icons.restaurant_menu,
                size: 22,
                color: failed ? AppColors.danger : AppColors.deepAmber,
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    label,
                    style: Theme.of(context)
                        .textTheme
                        .titleSmall
                        ?.copyWith(fontWeight: FontWeight.w700, color: AppColors.ink),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    _statusLabel(plan),
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(
                          color: failed ? AppColors.danger : AppColors.muted,
                        ),
                  ),
                ],
              ),
            ),
            if (!failed && plan.totalCaloriesKcal != null)
              Text(
                '${plan.totalCaloriesKcal!.round()} kcal',
                style: Theme.of(context)
                    .textTheme
                    .titleSmall
                    ?.copyWith(fontWeight: FontWeight.w800, color: AppColors.deepAmber),
              )
            else
              const Icon(Icons.chevron_right, color: AppColors.muted),
          ],
        ),
      ),
    );
  }

  String _statusLabel(MealPlan plan) {
    if (plan.status == 'error') {
      return 'Generation failed';
    }
    final dayCount = plan.dayCount;
    final generatedBy = plan.generatedBy;
    final by = generatedBy == null ? '' : ' · $generatedBy';
    if (dayCount > 1) return '$dayCount days$by';
    return '${plan.items.length} items$by';
  }

  static const _months = [
    'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec',
  ];
  String _month(int m) => _months[m - 1];
}
