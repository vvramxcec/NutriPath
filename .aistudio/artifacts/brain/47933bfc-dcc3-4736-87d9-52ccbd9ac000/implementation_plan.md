# NutriPath: Lifesum-Style Multi-Step Onboarding Architecture

Transform NutriPath's onboarding experience into a polished, consumer-grade, multi-step flow inspired by Lifesum iOS design patterns, while preserving NutriPath's clinical nutrition validation, drug-nutrient interaction gates, and Indian food dataset.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions synthesize your confirmed preferences. Please review before proceeding.

- **Confirmed Decision 1 (Flow Fidelity)**: Implement the **full multi-step Lifesum experience** complete with inspirational hero screens, social proof badges, goal selections, multi-select lifestyle desires, biometrics, affirmation interstitial, animated calculation progress (0% to 100%), and final plan reveal.
- **Confirmed Decision 2 (Clinical Integration)**: Seamlessly embed **dedicated clinical steps** for chronic medical conditions (Type 2 Diabetes, Hypertension, CKD, etc.) and food allergies (Peanuts, Dairy, Gluten, etc.) directly into the flow without compromising the sleek consumer aesthetic.
- **Confirmed Decision 3 (Visual Identity)**: Adopt the **Lifesum warm cream background (`#FDFBF7`) with vibrant emerald green buttons (`#22C55E` / `#16A34A`)**, clean stone typography, thin dark top progress bar, and modern selectable cards.
- **Confirmed Decision 4 (Authentication & Guest Continuity)**: Maintain optional guest access on the final plan reveal screen alongside one-tap Google Sign-In and Email/Password registration.

---

## 1. Flow Breakdown & Screen Stages

```
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 1: Welcome & Social Proof                                        │
│ • Hero photography asset + "Nutrition is your superpower for change"   │
│ • Credibility badges: "App of the Day", "Clinical Safety Engine"       │
│ • Action: [ GET STARTED ] or [ I already have an account ]             │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 2: Weight Goal Selection                                         │
│ • "How do you feel about your weight?"                                 │
│ • [ I want to lose weight ]                                            │
│ • [ Fine, but want to eat healthy ]                                    │
│ • [ I want more muscle mass ]                                          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 3: Lifestyle Desires (Multi-Select)                              │
│ • "Choose what else is important to you"                               │
│ • Selectable cards that turn vibrant green when active:                │
│   [ More daily energy ]        [ Stabilize my mood & blood sugar ]     │
│   [ Better fitting clothes ]   [ Create better structure ]             │
│   [ Learn healthy recipes ]    [ Feel mentally sharp ]                 │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 4: Biological Biometrics                                         │
│ • 4A: Gender Selection ("Female" / "Male")                             │
│ • 4B: Date of Birth / Age selector                                     │
│ • 4C: Weight & Height selector (kg / lbs unit switch)                  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 5: Dedicated Clinical Safety (NutriPath Core)                    │
│ • 5A: Medical Conditions (Type 2 Diabetes, Hypertension, CKD, etc.)    │
│ • 5B: Food Allergies & Sensitivities (Peanuts, Dairy, Gluten, etc.)    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 6: Affirmation Interstitial                                      │
│ • Soft card overlay: "Great — we've got your starting point.           │
│   Now we can make your plan smarter and more personal."                │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 7: Desired Speed of Change                                       │
│ • "Desired speed of change?"                                           │
│ • Slider (0.10 to 1.00 kg/week) with dynamic label:                    │
│   "0.20 kg/week • Lasting impact" vs "0.50 kg/week • Steady momentum"  │
│ • Action: [ BUILD MY PLAN ]                                            │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 8: Animated Calculation & Plan Synthesis                         │
│ • Full-screen pastel background with large percentage animation:       │
│   0% ──> 42% ──> 78% ──> 100%                                          │
│ • Dynamic clinical status text: "Calculating metabolic rate...",       │
│   "Applying ICMR clinical parameters...", "Finalizing your plan"       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│ Stage 9: Personalized Plan Reveal & Entry                              │
│ • "Your personalized plan is ready"                                    │
│ • Summary badge: Calorie target & Macro breakdown                      │
│ • [ Continue with Google ]                                             │
│ • [ Continue with Email ]                                              │
│ • [ Continue as Guest ]                                                │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Visual Styling & Color System

- **Background Canvas**: `#FDFBF7` (Lifesum Warm Cream / Vanilla)
- **Primary Action Buttons**: `#22C55E` (Vibrant Emerald Green) with bold white text
- **Selected Card Surface**: `#22C55E` or `#E8F8EE` with `#16A34A` outline
- **Unselected Card Surface**: `#FFFFFF` with subtle `#E7E5E4` border
- **Headings & Body Text**: `#1C1917` (Deep Charcoal Stone)
- **Secondary Subtitles**: `#78716C` (Warm Stone Muted)
- **Top Header Bar**: Minimalist back arrow + thin segmented progress bar tracking completion percentage.
- **Touch Target Standard**: 52dp height for all buttons and option cards.

---

## 3. Technical Architecture & Component Structure

### New / Updated Files
1. **`app/src/main/java/com/example/ui/screens/onboarding/LifesumOnboardingScreen.kt`**:
   - Master state machine coordinating the 9 onboarding stages.
   - Smooth `AnimatedContent` horizontal transitions between steps.
   - Hardware back button handling (`BackHandler`) to navigate backwards within the onboarding sequence.
2. **`app/src/main/java/com/example/ui/screens/onboarding/components/`**:
   - `OnboardingTopBar.kt`: Clean back arrow + animated thin progress bar.
   - `SelectableOptionCard.kt`: Standardized card with checkmark/radio styling and smooth elevation transitions.
   - `WheelNumberPicker.kt` or `UnitSlider.kt`: Polished numeric input for age, weight, and height.
   - `SpeedOfChangeSlider.kt`: Smooth slider with dynamic pace descriptors ("Lasting impact", "Steady momentum", "Accelerated pace").
   - `PlanCalculationAnimation.kt`: Coroutine-driven counter animating from 0 to 100% with staggered clinical status messages.
3. **`app/src/main/java/com/example/ui/theme/Color.kt` & `Theme.kt`**:
   - Add Lifesum color definitions (`LifesumCreamBackground`, `LifesumVibrantGreen`, `LifesumDarkStone`, `LifesumCardWhite`).
4. **`app/src/main/java/com/example/MainActivity.kt`**:
   - Wire `OnboardingScreen` to the new `LifesumOnboardingScreen`, connecting Google Sign-In, Email Sign-In, and guest completion callbacks.

---

## 4. Verification Plan

1. **Flow & Step Verification**:
   - Test step transitions from Welcome -> Goal -> Desires -> Biometrics -> Clinical -> Affirmation -> Speed -> Animation -> Reveal.
2. **Clinical Calculation Integrity**:
   - Verify that Mifflin-St Jeor BMR, TDEE, daily calorie deficit/surplus (based on the chosen speed of change), and medical conditions properly update `HealthProfile`.
3. **Authentication & Guest Mode**:
   - Test "Continue with Google", "Continue with Email", and "Continue as Guest" from the final reveal screen.
4. **Compilation & Unit Tests**:
   - Run `compile_applet` and Gradle test suite (`gradle :app:testDebugUnitTest`) to ensure 100% build health.
