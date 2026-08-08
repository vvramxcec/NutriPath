import 'package:flutter/material.dart';
import 'package:uuid/uuid.dart';

import '../../core/theme/app_theme.dart';
import 'models.dart';

/// Step 1 — basic profile form.
class ProfileSetupStep extends StatefulWidget {
  const ProfileSetupStep({
    super.key,
    required this.formKey,
    required this.initial,
    required this.onChanged,
  });

  final GlobalKey<FormState> formKey;
  final DemoProfile? initial;
  final ValueChanged<DemoProfile> onChanged;

  @override
  State<ProfileSetupStep> createState() => _ProfileSetupStepState();
}

class _ProfileSetupStepState extends State<ProfileSetupStep> {
  static const _uuid = Uuid();

  late final _name = TextEditingController(text: widget.initial?.displayName ?? '');
  late final _age = TextEditingController(text: widget.initial?.age?.toString() ?? '');
  late final _height = TextEditingController(text: widget.initial?.heightCm?.toString() ?? '');
  late final _weight = TextEditingController(text: widget.initial?.weightKg?.toString() ?? '');
  String? _sex;
  String? _activity;

  @override
  void initState() {
    super.initState();
    _sex = widget.initial?.sex;
    _activity = widget.initial?.activityLevel;
  }

  @override
  void dispose() {
    _name.dispose();
    _age.dispose();
    _height.dispose();
    _weight.dispose();
    super.dispose();
  }

  void _emit() {
    widget.onChanged(DemoProfile(
      id: widget.initial?.id ?? _uuid.v4(),
      displayName: _name.text.trim().isEmpty ? null : _name.text.trim(),
      age: int.tryParse(_age.text),
      sex: _sex,
      heightCm: double.tryParse(_height.text),
      weightKg: double.tryParse(_weight.text),
      activityLevel: _activity,
    ));
  }

  @override
  Widget build(BuildContext context) {
    final input = Theme.of(context).textTheme.bodyMedium;
    return SingleChildScrollView(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      child: Form(
        key: widget.formKey,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Let\'s get to know you', style: Theme.of(context).textTheme.headlineSmall?.copyWith(fontWeight: FontWeight.w700)),
            const SizedBox(height: 4),
            Text(
              'This stays on your device and drives your personalized meal plans.',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(color: AppColors.muted),
            ),
            const SizedBox(height: 20),
            TextFormField(
              controller: _name,
              onChanged: (_) => _emit(),
              textCapitalization: TextCapitalization.words,
              decoration: const InputDecoration(labelText: 'Name'),
              validator: (v) => (v == null || v.trim().isEmpty) ? 'Enter your name' : null,
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    controller: _age,
                    onChanged: (_) => _emit(),
                    keyboardType: TextInputType.number,
                    decoration: const InputDecoration(labelText: 'Age'),
                    validator: (v) {
                      final n = int.tryParse(v ?? '');
                      return (n == null || n < 1 || n > 120) ? '1–120' : null;
                    },
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: DropdownButtonFormField<String>(
                    initialValue: _sex,
                    decoration: const InputDecoration(labelText: 'Sex'),
                    items: const [
                      DropdownMenuItem(value: 'male', child: Text('Male', style: TextStyle(color: AppColors.ink))),
                      DropdownMenuItem(value: 'female', child: Text('Female', style: TextStyle(color: AppColors.ink))),
                      DropdownMenuItem(value: 'other', child: Text('Other', style: TextStyle(color: AppColors.ink))),
                    ],
                    onChanged: (v) => setState(() { _sex = v; _emit(); }),
                    validator: (v) => v == null ? 'Required' : null,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: TextFormField(
                    controller: _height,
                    onChanged: (_) => _emit(),
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(labelText: 'Height (cm)'),
                    validator: (v) {
                      final n = double.tryParse(v ?? '');
                      return (n == null || n <= 0 || n > 250) ? 'In cm' : null;
                    },
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: TextFormField(
                    controller: _weight,
                    onChanged: (_) => _emit(),
                    keyboardType: const TextInputType.numberWithOptions(decimal: true),
                    decoration: const InputDecoration(labelText: 'Weight (kg)'),
                    validator: (v) {
                      final n = double.tryParse(v ?? '');
                      return (n == null || n <= 0 || n > 400) ? 'In kg' : null;
                    },
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              initialValue: _activity,
              decoration: const InputDecoration(labelText: 'Activity level'),
              items: const [
                DropdownMenuItem(value: 'sedentary', child: Text('Sedentary', style: TextStyle(color: AppColors.ink))),
                DropdownMenuItem(value: 'light', child: Text('Lightly active', style: TextStyle(color: AppColors.ink))),
                DropdownMenuItem(value: 'moderate', child: Text('Moderately active', style: TextStyle(color: AppColors.ink))),
                DropdownMenuItem(value: 'active', child: Text('Very active', style: TextStyle(color: AppColors.ink))),
              ],
              onChanged: (v) => setState(() { _activity = v; _emit(); }),
              validator: (v) => v == null ? 'Required' : null,
            ),
            if (input != null) const SizedBox(height: 12),
          ],
        ),
      ),
    );
  }
}
