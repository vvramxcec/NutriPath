import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../shared/widgets/condition_chip.dart';
import 'profile_controller.dart';

/// Step 2 — multi-select health conditions.
class ConditionSelectionStep extends ConsumerWidget {
  const ConditionSelectionStep({
    super.key,
    required this.selectedIds,
    required this.onChanged,
  });

  final List<String> selectedIds;
  final ValueChanged<List<String>> onChanged;

  void _toggle(String id) {
    final next = selectedIds.contains(id)
        ? selectedIds.where((e) => e != id).toList()
        : [...selectedIds, id];
    onChanged(next);
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final conditions = ref.watch(conditionsProvider);
    return conditions.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Could not load conditions.\n$e')),
      data: (list) => ListView.separated(
        padding: const EdgeInsets.fromLTRB(20, 8, 20, 8),
        itemCount: list.length + 1,
        separatorBuilder: (_, __) => const SizedBox(height: 12),
        itemBuilder: (context, i) {
          if (i == 0) {
            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'What conditions are relevant?',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 4),
                Text(
                  'Pick any that apply — meal plans will respect the strictest limits.',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
                ),
              ],
            );
          }
          final c = list[i - 1];
          return ConditionChip(
            title: c.name,
            description: c.description ?? '',
            icon: c.icon,
            selected: selectedIds.contains(c.id),
            onTap: () => _toggle(c.id),
          );
        },
      ),
    );
  }
}
