import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../core/config/app_config.dart';
import '../../core/theme/app_theme.dart';
import '../../shared/widgets/medical_disclaimer_banner.dart';
import '../meal_plan/meal_plan_controller.dart';
import '../meal_plan/models.dart';
import '../onboarding/profile_controller.dart';

/// Main dashboard — warm gradient header with week selector + toggle, hero
/// calorie stat, macro stat row, and a tabbed "Daily Updates" section. Bottom
/// nav (Home / History / Profile) + black generate FAB.
class DashboardScreen extends ConsumerStatefulWidget {
  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  DateTime _selectedDate = DateTime.now();
  String _view = 'mealPlan'; // mealPlan | profile
  String _section = 'today'; // today | weekly

  @override
  Widget build(BuildContext context) {
    final profile = ref.watch(profileControllerProvider).valueOrNull;
    final planAsync = ref.watch(latestPlanProvider);
    final plan = planAsync.valueOrNull;
    final name = (profile?.displayName?.isNotEmpty ?? false)
        ? profile!.displayName!
        : 'there';

    return Scaffold(
      backgroundColor: AppColors.body,
      floatingActionButton: _GenerateFab(onTap: () => context.push('/dashboard/generate')),
      body: Column(
        children: [
          Expanded(
            child: SingleChildScrollView(
              child: Column(
                children: [
                  _Header(
                    name: name,
                    selectedDate: _selectedDate,
                    onSelectDate: (d) => setState(() => _selectedDate = d),
                    view: _view,
                    onViewChanged: (v) => setState(() => _view = v),
                    plan: plan,
                    profile: profile,
                  ),
                  if (_view == 'mealPlan')
                    _DailyUpdates(
                      plan: plan,
                      section: _section,
                      onSectionChanged: (s) => setState(() => _section = s),
                      loading: planAsync.isLoading,
                    )
                  else
                    _ProfileSummary(profile: profile, onEdit: () => context.push('/profile')),
                ],
              ),
            ),
          ),
          const MedicalDisclaimerBanner(),
          const _BottomNavBar(),
        ],
      ),
    );
  }
}

class _GenerateFab extends StatelessWidget {
  const _GenerateFab({required this.onTap});
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return FloatingActionButton(
      onPressed: onTap,
      tooltip: 'Generate a meal plan',
      child: const Icon(Icons.add, size: 28),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({
    required this.name,
    required this.selectedDate,
    required this.onSelectDate,
    required this.view,
    required this.onViewChanged,
    required this.plan,
    required this.profile,
  });

  final String name;
  final DateTime selectedDate;
  final ValueChanged<DateTime> onSelectDate;
  final String view;
  final ValueChanged<String> onViewChanged;
  final MealPlan? plan;
  final dynamic profile;

  @override
  Widget build(BuildContext context) {
    final day = selectedDate.weekday;
    final plan = this.plan; // local so null-check promotion applies
    final kcal = plan?.caloriesOnDay(day) ?? 0;
    return Container(
      decoration: const BoxDecoration(gradient: AppTheme.gradient),
      child: SafeArea(
        bottom: false,
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Expanded(
                    child: Text(
                      'Hi, $name',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                  IconButton(
                    onPressed: () => context.push('/profile'),
                    icon: const Icon(Icons.person_outline, color: Colors.white),
                  ),
                ],
              ),
              _WeekSelector(selectedDate: selectedDate, onSelect: onSelectDate),
              const SizedBox(height: 16),
              _SegmentedToggle(value: view, onChanged: onViewChanged),
              const SizedBox(height: 20),
              if (plan != null && plan.items.isNotEmpty) ...[
                _HeroNumber(value: kcal),
                const SizedBox(height: 6),
                const Text(
                  'kcal today',
                  style: TextStyle(color: Colors.white70, fontSize: 15),
                ),
                const SizedBox(height: 20),
                _MacroRow(plan: plan, day: day),
              ] else ...[
                const Text(
                  '—',
                  style: TextStyle(color: Colors.white, fontSize: 64, fontWeight: FontWeight.w700, height: 1.0),
                ),
                const SizedBox(height: 6),
                const Text(
                  'No meal plan yet',
                  style: TextStyle(color: Colors.white70, fontSize: 15),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class _WeekSelector extends StatelessWidget {
  const _WeekSelector({required this.selectedDate, required this.onSelect});
  final DateTime selectedDate;
  final ValueChanged<DateTime> onSelect;

  @override
  Widget build(BuildContext context) {
    final today = DateTime.now();
    final start = today.subtract(Duration(days: today.weekday - 1));
    final days = List.generate(7, (i) => start.add(Duration(days: i)));
    const labels = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];

    return SizedBox(
      height: 64,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        itemCount: days.length,
        separatorBuilder: (_, __) => const SizedBox(width: 8),
        itemBuilder: (context, i) {
          final d = days[i];
          final isToday = _sameDay(d, today);
          final isSelected = _sameDay(d, selectedDate);
          return GestureDetector(
            onTap: () => onSelect(d),
            child: Container(
              width: 46,
              decoration: BoxDecoration(
                color: isSelected ? Colors.white : Colors.white.withValues(alpha: 0.15),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    labels[i],
                    style: TextStyle(
                      color: isSelected ? AppColors.deepAmber : Colors.white,
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '${d.day}',
                    style: TextStyle(
                      color: isSelected ? AppColors.ink : Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  if (isToday)
                    Container(
                      width: 4,
                      height: 4,
                      margin: const EdgeInsets.only(top: 3),
                      decoration: const BoxDecoration(shape: BoxShape.circle, color: AppColors.deepAmber),
                    ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }

  bool _sameDay(DateTime a, DateTime b) =>
      a.year == b.year && a.month == b.month && a.day == b.day;
}

class _SegmentedToggle extends StatelessWidget {
  const _SegmentedToggle({required this.value, required this.onChanged});
  final String value;
  final ValueChanged<String> onChanged;

  @override
  Widget build(BuildContext context) {
    const options = {'mealPlan': 'Meal Plan', 'profile': 'Profile'};
    return Container(
      padding: const EdgeInsets.all(3),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.2),
        borderRadius: BorderRadius.circular(24),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          for (final e in options.entries)
            GestureDetector(
              onTap: () => onChanged(e.key),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 180),
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                decoration: BoxDecoration(
                  color: value == e.key ? Colors.white : Colors.transparent,
                  borderRadius: BorderRadius.circular(20),
                ),
                child: Text(
                  e.value,
                  style: TextStyle(
                    color: value == e.key ? AppColors.deepAmber : Colors.white,
                    fontWeight: FontWeight.w600,
                    fontSize: 13,
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _HeroNumber extends StatelessWidget {
  const _HeroNumber({required this.value});
  final double value;

  @override
  Widget build(BuildContext context) {
    return Text(
      value.round().toString(),
      style: HeroText.number(context),
    );
  }
}

class _MacroRow extends StatelessWidget {
  const _MacroRow({required this.plan, required this.day});
  final MealPlan plan;
  final int day;

  @override
  Widget build(BuildContext context) {
    final macros = [
      ('Protein', plan.proteinOnDay(day), 'g'),
      ('Carbs', plan.carbsOnDay(day), 'g'),
      ('Fat', plan.fatOnDay(day), 'g'),
    ];
    return Row(
      children: [
        for (final m in macros)
          Expanded(
            child: _MacroCard(label: m.$1, value: m.$2, unit: m.$3),
          ),
        if (macros.length < 3) const SizedBox(width: 12),
      ],
    );
  }
}

class _MacroCard extends StatelessWidget {
  const _MacroCard({required this.label, required this.value, required this.unit});
  final String label;
  final double value;
  final String unit;

  @override
  Widget build(BuildContext context) {
    return Container(
      margin: const EdgeInsets.only(right: 10),
      padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        boxShadow: AppTheme.cardShadow,
      ),
      child: Column(
        children: [
          Text(
            value.round().toString(),
            style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w700, color: AppColors.ink),
          ),
          const SizedBox(height: 2),
          Text('$label ($unit)', style: const TextStyle(fontSize: 11, color: AppColors.muted)),
        ],
      ),
    );
  }
}

class _DailyUpdates extends StatelessWidget {
  const _DailyUpdates({
    required this.plan,
    required this.section,
    required this.onSectionChanged,
    required this.loading,
  });

  final MealPlan? plan;
  final String section;
  final ValueChanged<String> onSectionChanged;
  final bool loading;

  @override
  Widget build(BuildContext context) {
    final plan = this.plan; // local so null-check promotion applies
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Daily updates',
            style: Theme.of(context)
                .textTheme
                .titleMedium
                ?.copyWith(fontWeight: FontWeight.w700, color: AppColors.ink),
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              _SectionTab('Today\'s Meals', section == 'today', () => onSectionChanged('today')),
              const SizedBox(width: 8),
              _SectionTab('Weekly Overview', section == 'weekly', () => onSectionChanged('weekly')),
            ],
          ),
          const SizedBox(height: 16),
          if (loading)
            const Padding(
              padding: EdgeInsets.all(24),
              child: Center(child: CircularProgressIndicator()),
            )
          else if (plan == null || plan.items.isEmpty)
            const _EmptyPlanCard()
          else if (section == 'today')
            _TodayMeals(plan: plan)
          else
            _WeeklyOverview(plan: plan),
        ],
      ),
    );
  }
}

class _SectionTab extends StatelessWidget {
  const _SectionTab(this.label, this.selected, this.onTap);
  final String label;
  final bool selected;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          color: selected ? AppColors.softGold : Colors.transparent,
          borderRadius: BorderRadius.circular(20),
        ),
        child: Text(
          label,
          style: TextStyle(
            color: selected ? AppColors.deepAmber : AppColors.muted,
            fontSize: 13,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }
}

class _EmptyPlanCard extends StatelessWidget {
  const _EmptyPlanCard();

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: AppTheme.cardRadius,
        boxShadow: AppTheme.cardShadow,
      ),
      child: Column(
        children: [
          const Icon(Icons.auto_awesome, size: 40, color: AppColors.deepAmber),
          const SizedBox(height: 10),
          Text(
            'Tap + to generate your personalized meal plan.',
            textAlign: TextAlign.center,
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(color: AppColors.muted),
          ),
        ],
      ),
    );
  }
}

class _TodayMeals extends StatelessWidget {
  const _TodayMeals({required this.plan});
  final MealPlan plan;

  @override
  Widget build(BuildContext context) {
    final items = plan.itemsByDay[DateTime.now().weekday] ?? const <MealPlanItem>[];
    if (items.isEmpty) {
      return const _EmptyPlanCard();
    }
    final byMeal = <String, List<MealPlanItem>>{};
    for (final it in items) {
      byMeal.putIfAbsent(it.mealType, () => []).add(it);
    }
    return Column(
      children: [
        for (final entry in byMeal.entries)
          Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: _MealCard(mealLabel: entry.value.first.mealLabel, items: entry.value),
          ),
      ],
    );
  }
}

class _MealCard extends StatelessWidget {
  const _MealCard({required this.mealLabel, required this.items});
  final String mealLabel;
  final List<MealPlanItem> items;

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
          Text(
            mealLabel,
            style: Theme.of(context)
                .textTheme
                .titleSmall
                ?.copyWith(fontWeight: FontWeight.w700, color: AppColors.ink),
          ),
          const SizedBox(height: 8),
          for (final it in items)
            Padding(
              padding: const EdgeInsets.only(bottom: 6),
              child: Row(
                children: [
                  Expanded(
                    child: Text(it.foodName, style: Theme.of(context).textTheme.bodyMedium),
                  ),
                  Text(
                    '${it.kcal.round()} kcal',
                    style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }
}

class _WeeklyOverview extends StatelessWidget {
  const _WeeklyOverview({required this.plan});
  final MealPlan plan;

  @override
  Widget build(BuildContext context) {
    final days = plan.itemsByDay.keys.toList()..sort();
    return Column(
      children: [
        for (final day in days)
          Padding(
            padding: const EdgeInsets.only(bottom: 10),
            child: Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: AppColors.card,
                borderRadius: AppTheme.cardRadius,
                boxShadow: AppTheme.cardShadow,
              ),
              child: Row(
                children: [
                  Text(
                    'Day $day',
                    style: const TextStyle(fontWeight: FontWeight.w600, color: AppColors.ink),
                  ),
                  const Spacer(),
                  Text(
                    '${plan.caloriesOnDay(day).round()} kcal',
                    style: const TextStyle(fontWeight: FontWeight.w700, color: AppColors.deepAmber),
                  ),
                ],
              ),
            ),
          ),
      ],
    );
  }
}

class _ProfileSummary extends StatelessWidget {
  const _ProfileSummary({required this.profile, required this.onEdit});
  final dynamic profile;
  final VoidCallback onEdit;

  @override
  Widget build(BuildContext context) {
    final p = profile;
    final bmi = computeBmi(weightKg: p?.weightKg, heightCm: p?.heightCm);
    return Padding(
      padding: const EdgeInsets.fromLTRB(20, 20, 20, 24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                'Your profile',
                style: Theme.of(context)
                    .textTheme
                    .titleMedium
                    ?.copyWith(fontWeight: FontWeight.w700, color: AppColors.ink),
              ),
              const Spacer(),
              TextButton(onPressed: onEdit, child: const Text('Edit')),
            ],
          ),
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
                  '${p?.displayName ?? 'Guest'} · ${p?.age ?? '?'} yrs',
                  style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 4),
                Text(
                  '${p?.heightCm?.toStringAsFixed(0) ?? '?'} cm · ${p?.weightKg?.toStringAsFixed(1) ?? '?'} kg · ${_activityLabel(p?.activityLevel)}',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
                ),
                const SizedBox(height: 8),
                Text(
                  bmi != null ? 'BMI ${bmi.toStringAsFixed(1)}' : 'BMI —',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w600),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  String _activityLabel(String? level) {
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
        return '—';
    }
  }
}

class _BottomNavBar extends StatelessWidget {
  const _BottomNavBar();

  @override
  Widget build(BuildContext context) {
    return Container(
      color: AppColors.card,
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _NavIcon(icon: Icons.home, active: true, onTap: () => context.go('/dashboard')),
          _NavIcon(icon: Icons.history, active: false, onTap: () => context.go('/history')),
          _NavIcon(icon: Icons.person_outline, active: false, onTap: () => context.go('/profile')),
        ],
      ),
    );
  }
}

class _NavIcon extends StatelessWidget {
  const _NavIcon({required this.icon, required this.active, required this.onTap});
  final IconData icon;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return IconButton(
      onPressed: onTap,
      icon: Icon(icon, color: active ? AppColors.inkSoft : AppColors.muted),
    );
  }
}
