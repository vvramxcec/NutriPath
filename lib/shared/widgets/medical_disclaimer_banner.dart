import 'package:flutter/material.dart';
import '../../core/theme/app_theme.dart';

/// Warm amber note pinned above the bottom nav: meal plans are informational,
/// not medical advice.
class MedicalDisclaimerBanner extends StatelessWidget {
  const MedicalDisclaimerBanner({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      color: AppColors.softGold.withValues(alpha: 0.35),
      child: Row(
        children: [
          const Icon(Icons.info_outline, size: 16, color: AppColors.deepAmber),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              'Meal plans are informational, not medical advice. Consult your doctor before changing your diet.',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(
                    color: AppColors.inkSoft,
                    height: 1.3,
                  ),
            ),
          ),
        ],
      ),
    );
  }
}
