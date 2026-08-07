import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/theme/app_theme.dart';
import '../../shared/widgets/condition_chip.dart';
import 'profile_controller.dart';

/// Step 3 — optional dietary restrictions (multi-select).
class RestrictionSelectionStep extends ConsumerWidget {
  const RestrictionSelectionStep({
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
    final restrictions = ref.watch(restrictionsProvider);
    return restrictions.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(child: Text('Could not load restrictions.\n$e')),
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
                  'Any dietary preferences?',
                  style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w700),
                ),
                const SizedBox(height: 4),
                Text(
                  'Optional — tap any that apply.',
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
                ),
              ],
            );
          }
          final r = list[i - 1];
          return ConditionChip(
            title: r.name,
            description: r.description ?? '',
            icon: Icons.restaurant_menu_outlined,
            selected: selectedIds.contains(r.id),
            onTap: () => _toggle(r.id),
          );
        },
      ),
    );
  }
}
