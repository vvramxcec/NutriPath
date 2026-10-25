# Personalized Gemini LLM Meal Plan Generator with Clinical Adherence Rationale

Integrate Google Gemini 3.5 Flash to generate personalized clinical meal plans directly from the user's saved health profile (biomarkers, conditions, medications, macro targets), paired with evidence-based adherence rationales, while maintaining the existing deterministic clinical engine as an automatic zero-downtime fallback.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> **Key Decisions & Revised Strategy:**
> - **Zero-Effort User Input**: The Gemini generator automatically reads all parameters from the user's existing saved `HealthProfile` (age, biological sex, BMI, chronic diseases, active medications, dietary restrictions, and clinically computed macro/micronutrient limits). The user does NOT need to re-enter any health details.
> - **Deterministic Engine as Robust Fallback**: The existing on-device `MealRecommendationEngine` (implementing DASH, KDIGO, ADA, and FDA DNI rules) serves as the primary fallback. If the device is offline, the network fails, or the Gemini API key is missing, NutriPath seamlessly generates meals via the local engine without error dialogs or broken states.
> - **Clinical Adherence Rationale**: For every meal synthesized by Gemini, the LLM provides an explicit, patient-tailored rationale explaining how the meal accommodates their specific chronic conditions and medications, fostering psychological and behavioral adherence.
> - **Dual Generation Modes**: A prominent switch allows the patient to select either **"Gemini LLM (Personalized AI)"** or **"Clinical Deterministic (Standard)"**, with an optional prompt bar for spontaneous cravings (e.g., *"Comforting soup under 500 calories"*).

---

### 1. Overview & Core Concept

- **What It Does**: Reads the patient's existing in-app health profile and calls Gemini 3.5 Flash to synthesize dynamic, diverse meals that strictly respect the patient's caloric and macronutrient boundaries. Every meal includes precise ingredients, preparation instructions, complete nutritional breakdown, and a tailored clinical rationale.
- **Target Audience / Persona**: Chronic disease patients (Type 2 Diabetes, Hypertension, CKD stages 1–5, CVD, Gout, Hypothyroidism, Dyslipidemia) who want varied, appetizing recipes that explain why the ingredients protect their kidneys, heart, or blood glucose.
- **Fallback Guarantee**: If offline or if the API request encounters issues, NutriPath falls back instantly to the local clinical catalog without interrupting the user's workflow.

---

### 2. User Experience & Visual Design

#### Key User Flows
1. **One-Tap Generation from Existing Profile**:
   - In `MealGeneratorScreen`, the user selects their target meal type (Breakfast, Lunch, Dinner, Snack, or Full Day).
   - The screen shows a badge: *"Using your saved profile: [Conditions] • [Medications] • [Target Calories]"*.
   - Tap **"Generate Meal Plan"** (or optionally type a custom note/craving).
2. **Generating State**:
   - Displays a clean Material 3 progress card: *"Gemini AI is analyzing your clinical profile and formulating personalized meals..."*.
3. **Displaying Generated Candidates & Adherence Rationale**:
   - Each recipe card displays:
     - Recipe name, prep time, calories, and color-coded macros (Protein, Carbs, Fat, Sodium, Potassium).
     - **"AI Clinical Rationale" Card**: High-contrast, soft-teal container detailing why this meal aligns with the patient's specific health condition (e.g., *"High soluble fiber from oats slows glucose absorption for Type 2 Diabetes; low-sodium preparation preserves renal perfusion"*).
     - **Adherence Behavioral Tips**: 2–3 actionable tips (e.g., flavor substitution ideas instead of table salt).
4. **Interactive Actions**:
   - **"Log as Consumed"**: Instantly records into daily tracking with live macro ring updates.
   - **"Adjust Portion Size"**: Opens the `AdjustPortionDialog` with live macro scaling.
   - **"Clinical ADIME Note"**: Generates a standardized clinical chart note for the meal.

#### Visual Identity & Theme
- **Color Tokens**:
  - `DeepTealPrimary` (`#005C53`): Core clinical brand color.
  - `LimeYellowAccent` (`#9FC131`): Highlight badge for AI-generated recommendations and sparkle indicators.
  - `WarmWhiteCard` (`#FDFBF7`): Card backgrounds for maximum legibility.
  - `StatusWarningAmber` (`#D97706`): Alert styling for potential safety contraindications.
- **Accessibility & Spacing**:
  - Touch targets minimum 48dp x 48dp.
  - Generous 12dp–16dp card padding adhering strictly to Material 3 density standards.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct Profile Injection into Gemini System Prompt**
  - *Chosen Approach*: The system prompt automatically formats the user's existing `HealthProfile` (stored in Room DB / `NutriPathViewModel`) into a structured clinical context block.
  - *Why*: Eliminates user friction—the user sets up their profile once, and all AI features automatically align with their clinical needs.
  - *Alternatives Considered*: Asking the user to re-select conditions before each generation was rejected as redundant and burdensome.

- **Decision 2: Automated Hybrid Fallback Mechanism**
  - *Chosen Approach*: The repository wraps the Gemini REST call in a resilient try-catch. If `GEMINI_API_KEY` is blank, network is unreachable, or a timeout occurs:
    1. It automatically delegates to `MealRecommendationEngine.generateRecommendations(...)`.
    2. Displays a subtle informational toast / chip: *"Showing clinical catalog recommendations (Offline Fallback)"*.
  - *Why*: Guarantees the app never presents an empty state or error screen during demonstrations or offline use.

- **Decision 3: Post-Generation Clinical Safety Screen**
  - *Chosen Approach*: Even though Gemini is instructed with clinical constraints, the returned recipe is passed through `MealSafetyValidator` to verify that drug-nutrient interactions (e.g., Warfarin + high Vitamin K foods like kale/spinach in excess) and renal nutrient bounds are safe before saving to the meal database.
  - *Why*: True medical safety requires zero-trust verification of generative AI outputs.

---

### 4. Technical Architecture & Data Strategy

#### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose Presentation                    │
│  ┌────────────────────────┐         ┌────────────────────────────────┐ │
│  │   MealGeneratorScreen  │ ◄─────► │   Profile Context Summary Bar  │ │
│  │  - Mode: AI / Standard │         │   (Pre-loaded from Room DB)    │ │
│  │  - Candidate Cards     │         │                                │ │
│  │  - Clinical Rationale  │         │  - Optional Craving Input      │ │
│  └───────────┬────────────┘         └────────────────────────────────┘ │
└──────────────┼─────────────────────────────────────────────────────────┘
               │
               ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    NutriPathViewModel & Repository                     │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ fun generatePersonalizedMeal(mealType, optionalCraving)          │  │
│  │  1. Extract active HealthProfile from StateFlow                  │  │
│  │  2. Attempt GeminiApiService.generateContent(...)                │  │
│  │  3. ON FAILURE -> Fallback to MealRecommendationEngine          │  │
│  │  4. Run MealSafetyValidator on generated recipes                 │  │
│  └───────────┬───────────────────────────────────────┬──────────────┘  │
└──────────────┼───────────────────────────────────────┼─────────────────┘
               │ Success                               │ Failure / Offline
┌──────────────▼─────────────┐              ┌──────────▼──────────────┐
│     GeminiApiService       │              │ MealRecommendationEngine│
│  POST v1beta/models/       │              │ (Deterministic Fallback)│
│  gemini-3.5-flash:generate │              │ - 27 Curated Recipes    │
│  - Structured JSON Output  │              │ - Hard DNI Filter       │
│  - Clinical Rationale      │              │ - ADIME Record Builder  │
└──────────────┬─────────────┘              └──────────┬──────────────┘
               │                                       │
               └───────────────────┬───────────────────┘
                                   │
                                   ▼
                      ┌────────────────────────┐
                      │  MealSafetyValidator   │
                      │  (Safety Verification) │
                      └────────────┬───────────┘
                                   │ Verified MealCandidate
                            ┌──────▼──────┐
                            │ Room DB     │
                            │ MealPlanDAO │
                            └─────────────┘
```

#### Prompt Engineering & Structured Response
The Gemini prompt will receive:
```
Patient Profile:
- Age: {profile.age}, Sex: {profile.biologicalSex}, Weight: {profile.weightKg}kg, Height: {profile.heightCm}cm
- Conditions: {profile.chronicDiseases}
- Medications: {profile.activeMedications}
- Daily Targets: {profile.targetCalories} kcal (Protein: {targetProtein}g, Carbs: {targetCarbs}g, Fat: {targetFat}g)
- Dietary Restrictions: {profile.dietaryPreferences}, Allergies: {profile.allergies}
- Meal Requested: {mealType}
- Special Notes: {optionalCraving}

Generate a personalized recipe in JSON format strictly matching this schema:
{
  "mealName": "...",
  "mealType": "...",
  "description": "...",
  "prepTimeMinutes": 25,
  "calories": 420.0,
  "proteinGrams": 28.0,
  "carbsGrams": 45.0,
  "fatGrams": 14.0,
  "fiberGrams": 9.0,
  "sodiumMg": 380.0,
  "potassiumMg": 420.0,
  "ingredients": [{"name": "...", "amount": "...", "unit": "..."}],
  "instructions": ["Step 1...", "Step 2..."],
  "clinicalRationale": "Detailed clinical reasoning on how this meal assists the patient's conditions and promotes dietary adherence...",
  "adherenceTips": ["Behavioral tip 1...", "Behavioral tip 2..."]
}
```

#### Step-by-Step Implementation Workflow
1. **Secret & Config Setup**:
   - Uncomment `GEMINI_API_KEY` in `.env.example`.
   - Ensure `<uses-permission android:name="android.permission.INTERNET" />` in `AndroidManifest.xml`.
2. **Network & Service Layer**:
   - Implement `GeminiApiService` using existing Retrofit and Moshi dependencies.
   - Implement `GeminiMealRepository` with structured prompt formatting, network call execution, JSON mapping, and automatic fallback to `MealRecommendationEngine`.
3. **ViewModel Integration**:
   - Expose `isAiGenerating`, `generationMode`, and `lastAdherenceRationale` in `NutriPathViewModel`.
   - Implement `generatePersonalizedMeals(mealType, customNotes)` reading from `_healthProfile.value`.
4. **UI Updates (`MealGeneratorScreen.kt`)**:
   - Add the generation mode selector (Gemini AI vs. Deterministic Catalog).
   - Display the active profile context summary so users see what health parameters the AI is using.
   - Add the clinical adherence rationale card to each generated recipe.
   - Retain full compatibility with "Log as Consumed", portion adjustments, and ADIME charting.
5. **Compilation & Build Verification**:
   - Run `compile_applet` to ensure clean compilation and zero regressions.
