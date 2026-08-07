import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import 'config/app_config.dart';

/// The Supabase client (anon key, permissive RLS demo mode).
final supabaseClientProvider = Provider<SupabaseClient>((ref) {
  if (!AppConfig.isConfigured) {
    throw StateError(
      'Supabase is not configured. Run with '
      '--dart-define=VITE_SUPABASE_URL=... --dart-define=VITE_SUPABASE_ANON_KEY=...',
    );
  }
  return Supabase.instance.client;
});
