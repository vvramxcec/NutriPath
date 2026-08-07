import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../features/onboarding/profile_controller.dart';
import 'config/app_config.dart';
import 'router/app_router.dart';
import 'theme/app_theme.dart';

/// Root widget: configures Supabase, wires the router, and refreshes redirects
/// when the profile state changes.
class NutriPathApp extends ConsumerWidget {
  const NutriPathApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    if (!AppConfig.isConfigured) {
      return const MaterialApp(
        debugShowCheckedModeBanner: false,
        home: _ConfigRequiredScreen(),
      );
    }

    final router = ref.watch(appRouterProvider);
    // Re-evaluate redirects whenever the profile (onboarded state) changes.
    ref.listen(profileControllerProvider, (_, __) => router.refresh());

    return MaterialApp.router(
      title: 'NutriPath',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light(),
      routerConfig: router,
    );
  }
}

/// Cold-start splash while the demo profile loads from preferences.
class SplashScreen extends StatelessWidget {
  const SplashScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(gradient: AppTheme.gradient),
        child: const Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.spa, size: 56, color: Colors.white),
              SizedBox(height: 12),
              Text(
                'NutriPath',
                style: TextStyle(
                  color: Colors.white,
                  fontSize: 28,
                  fontWeight: FontWeight.w700,
                ),
              ),
              SizedBox(height: 24),
              CircularProgressIndicator(color: Colors.white),
            ],
          ),
        ),
      ),
    );
  }
}

class _ConfigRequiredScreen extends StatelessWidget {
  const _ConfigRequiredScreen();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.settings_suggest_outlined, size: 56, color: AppColors.deepAmber),
            const SizedBox(height: 16),
            Text('NutriPath needs configuration', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 12),
            const Text(
              'Run the app with your Supabase project details:\n\n'
              'flutter run \\\n'
              '  --dart-define=VITE_SUPABASE_URL=https://your-ref.supabase.co \\\n'
              '  --dart-define=VITE_SUPABASE_ANON_KEY=your-anon-key',
              textAlign: TextAlign.center,
              style: TextStyle(fontFamily: 'monospace', fontSize: 12),
            ),
          ],
        ),
        ),
      ),
    );
  }
}
