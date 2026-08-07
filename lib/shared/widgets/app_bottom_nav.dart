import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';

import '../../core/theme/app_theme.dart';

/// The 3-icon bottom nav (Home / History / Profile) shared by the main screens.
class AppBottomNav extends StatelessWidget {
  const AppBottomNav({super.key, required this.current});
  final int current; // 0 = Home, 1 = History, 2 = Profile

  @override
  Widget build(BuildContext context) {
    return Container(
      color: AppColors.card,
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _NavItem(
            icon: Icons.home,
            active: current == 0,
            onTap: () => context.go('/dashboard'),
          ),
          _NavItem(
            icon: Icons.history,
            active: current == 1,
            onTap: () => context.go('/history'),
          ),
          _NavItem(
            icon: Icons.person_outline,
            active: current == 2,
            onTap: () => context.go('/profile'),
          ),
        ],
      ),
    );
  }
}

class _NavItem extends StatelessWidget {
  const _NavItem({required this.icon, required this.active, required this.onTap});
  final IconData icon;
  final bool active;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return IconButton(
      onPressed: onTap,
      tooltip: active ? null : (icon == Icons.home ? 'Home' : icon == Icons.history ? 'History' : 'Profile'),
      icon: Icon(icon, color: active ? AppColors.inkSoft : AppColors.muted),
    );
  }
}
