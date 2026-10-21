# NutriPath: User Authentication & Account Management Architecture

Enable secure user authentication (Login, Logout, and Account Deletion) supporting both Google Sign-In via Android Credential Manager and Email/Password with Firebase Auth, maintaining optional guest usage and complete local & cloud data wipe upon account deletion.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions synthesize your confirmed preferences from Phase 1. Please review before proceeding.

- **Confirmed Decision 1 (Auth Methods)**: Support **both Google Sign-In (via Credential Manager) and Email/Password** authentication through Firebase Auth. Google Sign-In provides one-tap, seamless biometric authentication, while Email/Password provides universal access.
- **Confirmed Decision 2 (Gating Strategy)**: **Optional Guest Access with Cloud Sync**. Users can continue using NutriPath offline as a guest without friction; signing in connects their profile to the cloud for backup and sync across devices.
- **Confirmed Decision 3 (Account Deletion Scope)**: **Full Cloud & Local Purge**. When a user deletes their account, the app deletes their Firebase Auth identity, purges their cloud records, completely wipes the local Room SQLite database (`HealthProfile`, `MealPlanEntity`), and returns the app to a clean onboarding zero-state.
- **Confirmed Decision 4 (Clinical Core & Dataset Integrity)**: All underlying deterministic nutrition calculations, NutriOrion Drug-Nutrient Interaction safety gates, and the 1,015-item Indian food dataset in Room SQLite remain intact and operate seamlessly in both guest and authenticated states.

---

## 1. Overview & Core Concept

### What It Does
* **Guest & Authenticated States**: Users can seamlessly use the app without logging in (storing all profiles, meals, and logs locally in Room SQLite). When ready, they can sign in or sign up via Google Sign-In or Email/Password.
* **Account Hub (`AccountScreen` / Menu Integration)**: In `AppMenuScreen`, an "Account & Sync" section shows the active user state (Signed In as `user@example.com` vs. "Guest / Local Mode"), with action buttons to Log In, Log Out, or Delete Account.
* **Authentication Dialog / Screen (`AuthDialog` / `AuthScreen`)**: Clean M3 bottom sheet or dialog offering:
  1. "Sign in with Google" button (using Jetpack Credential Manager `GetSignInWithGoogleOption`).
  2. Email and Password fields with "Sign In" and "Create Account" toggle.
* **Account Deletion Safeguard**: A double-confirmation modal warning the user that deleting their account irreversibly destroys their cloud profile and erases all local meal plans and logs.

---

## 2. User Experience & Visual Design

### User Interaction Flows

```
┌──────────────────────────────────────────────────────────────┐
│  Home Screen (Natural Header)                                │
│  [NP] NutriPath                           [ ⋮ (Menu) ]       │
└───────────────────────────────┬──────────────────────────────┘
                                │ Taps 3-dot button
                                ▼
┌──────────────────────────────────────────────────────────────┐
│  AppMenuScreen ("Menu & Clinical Settings")                  │
│                                                              │
│  ┌────────────────────────────────────────────────────────┐  │
│  │ 👤 Account & Cloud Backup                              │  │
│  │ Status: Signed in as vishnu@gmail.com                  │  │
│  │ [ Log Out ]                     [ Delete Account ]     │  │
│  │ (Or if Guest: "Guest Mode" -> [ Sign In / Register ])  │  │
│  └────────────────────────────────────────────────────────┘  │
│                                                              │
│  • Health Profile                                            │
│  • Clinical Safety & DNI Rules                               │
│  • Re-run Health Setup                                       │
│  • Clinical Engine Information                               │
└───────────────────────────────┬──────────────────────────────┘
                                │ Taps "Delete Account"
                                ▼
┌──────────────────────────────────────────────────────────────┐
│  AlertDialog: "Delete Account & Purge Data?"                 │
│  "This will permanently delete your cloud account and wipe   │
│   all local meal history and health profiles.                │
│   This action cannot be undone."                             │
│                                                              │
│  [ Cancel ]                 [ Delete Account & Wipe (Red) ]  │
└──────────────────────────────────────────────────────────────┘
```

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Dual Identity Providers (Google Sign-In + Email/Password)**
  * *Chosen Approach*: Implement Credential Manager for Google Sign-In (one-tap biometric) alongside Firebase Email/Password Auth.
  * *Why*: Meets your explicit requirement for both authentication options while providing the smoothest native Android sign-in UX.
- **Decision 2: Non-Blocking Guest Architecture**
  * *Chosen Approach*: Keep the app functional offline out-of-the-box using Room SQLite. Signing in attaches a Firebase `uid` and syncs the profile to Firestore.
  * *Why*: Medical nutrition apps should never lock a patient out during acute meal logging or grocery shopping due to network dropout.
- **Decision 3: Complete Data Cascade on Deletion**
  * *Chosen Approach*: Execute `user.delete()` on Firebase Auth, and trigger `viewModel.clearAllData()` to empty the Room database and restart onboarding.
  * *Why*: Strictly adheres to Google Play privacy policies and patient data deletion rights (GDPR/HIPAA principles).

---

## 4. Technical Architecture & Implementation Strategy

```
┌──────────────────────────────────────────────────────────────────────────┐
│                         Presentation Layer                               │
│                                                                          │
│    HomeScreen ──(⋮)──> AppMenuScreen ───> AuthDialog / Screen            │
│                             │                                            │
│                             ├──> HealthProfileScreen                     │
│                             └──> SettingsSafetyScreen                    │
└──────────────────────────────┬───────────────────────────────────────────┘
                               │
                               ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                     ViewModel & Auth Repository                          │
│                                                                          │
│  • NutriPathViewModel                                                    │
│    - currentUser: StateFlow<FirebaseUser?>                               │
│    - signInWithGoogle(credential: CustomCredential)                      │
│    - signInWithEmail(email, password)                                    │
│    - signUpWithEmail(email, password)                                    │
│    - signOut()                                                           │
│    - deleteAccountAndPurgeData()                                         │
│                                                                          │
│  • AuthRepository                                                        │
│    - FirebaseAuth.getInstance()                                          │
│    - CredentialManager (GetSignInWithGoogleOption)                       │
└──────────────────────────────┬───────────────────────────────────────────┘
                               │
               ┌───────────────┴───────────────┐
               ▼                               ▼
┌─────────────────────────────┐ ┌──────────────────────────────────────────┐
│   Firebase Cloud Backend    │ │          Local Room SQLite DB            │
│                             │ │                                          │
│ • Firebase Auth (User Id)   │ │ • HealthProfileDao (Profile Data)        │
│ • Firestore (Cloud Backup)  │ │ • MealPlanDao (Daily Meals & History)    │
│                             │ │ • IndianFoodDao (1,015 Reference Items)  │
└─────────────────────────────┘ └──────────────────────────────────────────┘
```

### Steps to Execute Upon Approval
1. **Firebase Setup (`set_up_firebase`)**:
   * Execute standard 2-step setup handshake to provision Firebase Authentication and Firestore on the backend.
2. **Dependencies in `build.gradle.kts`**:
   * Enable `libs.firebase.auth`, `libs.androidx.credentials`, `libs.androidx.credentials.play.services`, `libs.googleid`, and `libs.firebase.firestore`.
3. **Authentication Repository & Services**:
   * Implement `AuthRepository` managing `FirebaseAuth`, `CredentialManager`, Google Sign-In token exchanges, and Email/Password flows.
4. **ViewModel Integration**:
   * Connect `NutriPathViewModel` to observe `authStateFlow`, handle login/logout states, and cascade account deletion to `clearAllData()`.
5. **UI Components (`AuthDialog` & `AccountCard` in `AppMenuScreen`)**:
   * Build the clean M3 Authentication Dialog with Google Sign-In and Email/Password options.
   * Add the Account Status Card in `AppMenuScreen` with Log In, Log Out, and Delete Account triggers.
6. **Verification (`compile_applet`)**:
   * Compile and verify the complete application build.
