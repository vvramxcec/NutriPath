#!/usr/bin/env bash
# Dev launcher for NutriPath on this machine.
#
# The system Android SDK (/usr/lib/android-sdk) is root-owned, so AGP can't
# auto-install missing components there. All components (build-tools 35.0.0,
# platform android-36, platform-tools, NDK 27, CMake) live in a user-writable
# SDK root instead. `flutter config --android-sdk` already points Flutter at it;
# the exports below are belt-and-suspenders for Gradle's own resolution.
#
# Usage:  ! bash tools/run_app.sh
#         ! bash tools/run_app.sh --profile     (extra args pass through)
set -euo pipefail

export ANDROID_HOME="${ANDROID_HOME:-/home/vvram/android-sdk-extras}"
export ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-/home/vvram/android-sdk-extras}"

# Demo-mode Supabase config (anon key is public by design — it ships in the app).
VITE_SUPABASE_URL=https://elrmbjfrojvzdudlrkvo.supabase.co
VITE_SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVscm1iamZyb2p2emR1ZGxya3ZvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODYxMTg5MjYsImV4cCI6MjEwMTY5NDkyNn0.kXDaeWAr-LLsGHhWN4UUab-n8T3bpYD79701L27Nk8A

cd "$(dirname "$0")/.."
exec flutter run \
  --dart-define=VITE_SUPABASE_URL="$VITE_SUPABASE_URL" \
  --dart-define=VITE_SUPABASE_ANON_KEY="$VITE_SUPABASE_ANON_KEY" \
  "$@"
