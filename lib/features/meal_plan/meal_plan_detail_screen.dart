import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import 'meal_plan_controller.dart';
import 'models.dart';

/// Full plan view — horizontal day tabs (gold underline), meals grouped by
/// meal type with calorie badges and amber "why" reason chips, plus a per-day
/// macro stat row.
class MealPlanDetailScreen extends ConsumerStatefulWidget {
  const MealPlanDetailScreen({super.key, required this.planId});
  final String planId;

  @override
  ConsumerState<MealPlanDetailScreen> createState() => _MealPlanDetailScreenState();
}

class _MealPlanDetailScreenState extends ConsumerState<MealPlanDetailScreen> {
  int _day = 1;

  @override
  Widget build(BuildContext context) {
    final planAsync = ref.watch(planByIdProvider(widget.planId));
    return Scaffold(
      backgroundColor: AppColors.body,
      appBar: AppBar(title: const Text('Your meal plan')),
      body: planAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Text('Could not load plan.\n$e', textAlign: TextAlign.center),
          ),
        ),
        data: (plan) {
          final dayCount = plan.dayCount;
          final safeDay = _day.clamp(1, dayCount > 0 ? dayCount : 1);
          return Column(
            children: [
              _DayTabs(dayCount: dayCount, selected: safeDay, onSelect: (d) => setState(() => _day = d)),
              Expanded(
                child: ListView(
                  padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
                  children: [
                    if (plan.summary != null) _SummaryCard(text: plan.summary!),
                    const SizedBox(height: 16),
                    for (final item in (plan.itemsByDay[safeDay] ?? const <MealPlanItem>[]))
                      Padding(
                        padding: const EdgeInsets.only(bottom: 12),
                        child: _DayMealCard(item: item),
                      ),
                  ],
                ),
              ),
              _DayMacros(plan: plan, day: safeDay),
            ],
          );
        },
      ),
    );
  }
}

class _DayTabs extends StatelessWidget {
  const _DayTabs({required this.dayCount, required this.selected, required this.onSelect});
  final int dayCount;
  final int selected;
  final ValueChanged<int> onSelect;

  @override
  Widget build(BuildContext context) {
    if (dayCount <= 1) return const SizedBox.shrink();
    return Container(
      color: AppColors.card,
      child: Row(
        children: [
          for (var d = 1; d <= dayCount; d++)
            Expanded(
              child: GestureDetector(
                onTap: () => onSelect(d),
                child: Column(
                  children: [
                    Padding(
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      child: Text(
                        'Day $d',
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                          color: selected == d ? AppColors.deepAmber : AppColors.muted,
                        ),
                      ),
                    ),
                    Container(
                      height: 3,
                      color: selected == d ? AppColors.deepAmber : Colors.transparent,
                    ),
                  ],
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _SummaryCard extends StatelessWidget {
  const _SummaryCard({required this.text});
  final String text;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.softGold.withValues(alpha: 0.3),
        borderRadius: AppTheme.cardRadius,
      ),
      child: Text(text, style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: AppColors.inkSoft)),
    );
  }
}

class _DayMealCard extends StatelessWidget {
  const _DayMealCard({required this.item});
  final MealPlanItem item;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: AppTheme.cardRadius,
        boxShadow: AppTheme.cardShadow,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(item.mealIcon, size: 20, color: AppColors.deepAmber),
              const SizedBox(width: 8),
              Text(
                item.mealLabel,
                style: Theme.of(context)
                    .textTheme
                    .titleSmall
                    ?.copyWith(fontWeight: FontWeight.w700, color: AppColors.ink),
              ),
              const Spacer(),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: AppColors.softGold.withValues(alpha: 0.35),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  '${item.kcal.round()} kcal',
                  style: const TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: AppColors.deepAmber),
                ),
              ),
            ],
          ),
          const SizedBox(height: 10),
          Text(
            item.foodName,
            style: Theme.of(context).textTheme.bodyLarge?.copyWith(fontWeight: FontWeight.w600),
          ),
          if (item.portion != null)
            Text(
              item.portion!,
              style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
            ),
          if (item.notes != null) ...[
            const SizedBox(height: 10),
            Wrap(
              spacing: 6,
              runSpacing: 6,
              children: [
                for (final note in _chunk(item.notes!))
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
                    decoration: BoxDecoration(
                      borderRadius: BorderRadius.circular(12),
                      border: Border.all(color: AppColors.softGold),
                    ),
                    child: Text(
                      note,
                      style: const TextStyle(fontSize: 11, color: AppColors.inkSoft),
                    ),
                  ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  List<String> _chunk(String text) {
    if (text.length <= 90) return [text];
    // simple split on sentence boundaries for display
    final parts = <String>[];
    var buf = StringBuffer();
    for (final sentence in text.split(RegExp(r'(?<=[.;]) '))) {
      if (buf.length + sentence.length > 90 && buf.isNotEmpty) {
        parts.add(buf.toString().trim());
        buf = StringBuffer();
      }
      buf.write(sentence);
      buf.write(' ');
    }
    if (buf.isNotEmpty) parts.add(buf.toString().trim());
    return parts;
  }
}

class _DayMacros extends StatelessWidget {
  const _DayMacros({required this.plan, required this.day});
  final MealPlan plan;
  final int day;

  @override
  Widget build(BuildContext context) {
    final macros = [
      ('kcal', plan.caloriesOnDay(day)),
      ('Protein', plan.proteinOnDay(day)),
      ('Carbs', plan.carbsOnDay(day)),
      ('Fat', plan.fatOnDay(day)),
    ];
    return Container(
      color: AppColors.card,
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 12),
      child: Row(
        children: [
          for (final m in macros)
            Expanded(
              child: Column(
                children: [
                  Text(
                    m.$2.round().toString(),
                    style: const TextStyle(fontWeight: FontWeight.w800, color: AppColors.ink),
                  ),
                  const SizedBox(height: 2),
                  Text(m.$1, style: const TextStyle(fontSize: 11, color: AppColors.muted)),
                ],
              ),
            ),
        ],
      ),
    );
  }
}
