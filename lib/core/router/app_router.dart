import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../features/meal_plan/meal_plan_detail_screen.dart';
import '../../features/onboarding/onboarding_screen.dart';
import '../../features/onboarding/profile_controller.dart';
import '../../features/dashboard/dashboard_screen.dart';
import '../../features/dashboard/history_screen.dart';
import '../../features/meal_plan/generation_screen.dart';
import '../../features/profile/profile_screen.dart';
import '../app_shell.dart';

/// go_router configuration with profile-state redirects (demo mode).
final appRouterProvider = Provider<GoRouter>((ref) {
  return GoRouter(
    initialLocation: '/',
    redirect: (context, state) {
      final profile = ref.read(profileControllerProvider).valueOrNull;
      final location = state.matchedLocation;

      if (profile == null) {
        // still loading, or onboarded=false
        if (location == '/splash' || location == '/onboarding') return null;
        return location == '/' ? '/splash' : '/onboarding';
      }
      // onboarded: only allow the app routes
      if (location == '/' || location == '/splash' || location == '/onboarding') {
        return '/dashboard';
      }
      return null;
    },
    routes: [
      GoRoute(path: '/splash', builder: (_, __) => const SplashScreen()),
      GoRoute(path: '/onboarding', builder: (_, __) => const OnboardingScreen()),
      GoRoute(path: '/dashboard', builder: (_, __) => const DashboardScreen()),
      GoRoute(path: '/dashboard/generate', builder: (_, __) => const GenerationScreen()),
      GoRoute(
        path: '/dashboard/plan/:id',
        builder: (_, state) =>
            MealPlanDetailScreen(planId: state.pathParameters['id']!),
      ),
      GoRoute(path: '/history', builder: (_, __) => const HistoryScreen()),
      GoRoute(path: '/profile', builder: (_, __) => const ProfileScreen()),
    ],
  );
});
