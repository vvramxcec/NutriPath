# NutriPath: Research-Backed Clinical Architecture & Anuvaad Dataset Integration

Personalized, clinically safe medical nutrition application integrating principles from HealthGenie, NutriOrion, and NutriGen alongside the 1,000+ Indian Food Micronutrient Dataset from Anuvaad.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following decisions synthesize the latest research papers (HealthGenie, NutriOrion, NutriGen) with your confirmed preferences from Phase 1. Please review before proceeding.

- **Confirmed Decision 1 (Anuvaad Dataset Delivery)**: The Anuvaad Indian Food composition dataset (1,000+ items with rich micronutrient profiles) will be provided via chat upload. The app will feature an extensible CSV ingestion parser and an initial rich pre-seeded Indian/Kerala food database (Matta rice, thoran, avial, sambar, ragi puttu, moong dal, etc.) that merges seamlessly with uploaded CSV records.
- **Confirmed Decision 2 (Dataset Utilization)**: The 1,000+ items will power both **Deterministic Recipe & Meal Generation** (NutriGen-style accurate nutritional calculations) and **Safety Validation Gates** (NutriOrion-style hard negative constraints and micronutrient boundary checks).
- **Confirmed Decision 3 (Three-Paper Research Synthesis)**:
  1. *HealthGenie (2504.14594)*: Circular interactive exploration where users can view visualized ingredient relationships and tap to Include or Exclude items with immediate re-planning, avoiding repetitive text queries.
  2. *NutriOrion (2602.18650)*: Preventing the **"Healthy Food Trap"** (e.g., banning high-Vitamin-K spinach/kale for Warfarin or high-potassium foods for CKD/potassium-sparing diuretics), utilizing Severity-Urgency-Modifiability (SUM) multi-objective conflict resolution for multimorbidities, and structuring clinical interventions via the ADIME standard.
  3. *NutriGen (2502.20601)*: Strict separation of AI meal creativity from deterministic nutritional calculations to eliminate caloric hallucination, generating 3 diverse daily options with exact portion sizes.

---

## 1. Overview & Core Concept

### What It Does
NutriPath delivers individualized, clinically grounded daily meal plans and recipe recommendations. Instead of generating abstract caloric suggestions or hallucinated nutrition tables, NutriPath anchors all recipes to verified food composition tables (ICMR-NIN, USDA, and Anuvaad's Indian Food Dataset). It subjects every meal to a dual-layer evaluation: an AI recommendation engine guided by clinical knowledge, and a deterministic safety validator that enforces strict pharmacological contraindications, allergy exclusions, and multi-objective clinical targets.

### Target Audience & Persona
- Adults managing chronic or comorbid conditions (Type 2 Diabetes, Hypertension, Chronic Kidney Disease, Hyperlipidemia, GERD).
- Individuals taking medications with strict food-drug interactions (e.g., Warfarin, Statins, ACE inhibitors, Potassium-sparing diuretics).
- Users with dietary restrictions (Celiac/Gluten-Free, Lactose Intolerance, Nut Allergies, Vegetarian/Vegan) seeking culturally authentic Indian and Kerala-style meals.

### Key Value
Clinically validated safety by construction: zero dangerous food recommendations, transparent "Why is this meal suitable?" explanations, and interactive ingredient control via circular feedback.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Onboarding & Multimorbidity Health Profile**:
   - Collects age, sex, weight, height, activity level, and specific diagnoses (Diabetes, Hypertension, CKD Stage 1-4, GERD, Dyslipidemia).
   - Captures medications (Warfarin, ACE inhibitors, Statins, Diuretics) and allergies/intolerances (Gluten, Peanuts, Dairy, Shellfish).
   - Generates personalized daily macro and micronutrient boundaries based on clinical guidelines (ADA, DASH, KDOQI).

2. **Daily Nutrition Dashboard (Home)**:
   - Progress arcs for calories, protein, carbs, fats, sodium, potassium, and fiber against clinically set targets.
   - Time-aware recommendation card suggesting the next meal (Breakfast, Lunch, Dinner, Snack) with immediate status indicators (`APPROVED`, `MODIFIED`, or `SAFETY WARNING`).

3. **Circular Interactive Meal Planner (HealthGenie Inspired)**:
   - Displays 3 distinct, culturally authentic meal options with exact weights (grams) and step-by-step preparation steps.
   - Interactive Ingredient Graph / Entity Chips: Users can tap any ingredient to **Include** (prefer) or **Exclude** (avoid).
   - Real-time recalculation: Toggling an ingredient dynamically adapts the recipe and updates the nutrient breakdown instantly without chat round-trips.

4. **Clinical Safety & ADIME Inspector (NutriOrion Inspired)**:
   - Tap any meal to view the clinical rationale card:
     - **Assessment**: Patient biomarkers and target nutrient thresholds.
     - **Diagnosis**: Detected risk factors (e.g., elevated glycemic index, sodium excess risk).
     - **Intervention**: Concrete actions (`Continue`, `Replace`, `Add`).
     - **Safety Gate**: Verifies absence of Drug-Nutrient Interactions (DNI) and allergen flags.

5. **Anuvaad Dataset & Food Explorer**:
   - Searchable directory of Indian food items with full macronutrient and micronutrient profiles (iron, calcium, potassium, sodium, fiber, vitamins).
   - Ingestion banner allowing direct upload and indexing of new Anuvaad CSV datasets.

### Visual Identity & Theme
- **Color Palette**:
  - Primary Background: Warm Lime Yellow (`#F4F8C2` / `#EFF6AA`), cheerful and health-focused.
  - Primary Action / Accent: Deep Medical Teal (`#0D5C5E` / `#0A4D4F`), high contrast, clinical credibility.
  - Cards & Surfaces: Warm Off-White (`#FCFDF5` / `#FFFFFF`), elevated with subtle borders (`#E0E5C1`).
  - Text: Dark Charcoal (`#1B1C18`) for body; High-contrast Deep Teal (`#0D5C5E`) for headings.
  - Status Indicators: Safe Green (`#1E824C`), Caution Amber (`#D97706`), Contraindication Red (`#DC2626`).
- **Typography & Hierarchy**: Clear, readable sans-serif typography with generous line heights and distinct headline hierarchies to prevent visual clutter and eye fatigue.
- **Layout & Accessibility**: 48dp minimum touch targets, high-contrast text ratios exceeding WCAG AAA, and clean Material 3 card grouping.

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Prevention of the "Healthy Food Trap" (NutriOrion Rule)
- **Problem Identified in Research**: LLMs frequently recommend universally praised "healthy" foods (spinach, kale, avocado, bananas) to cardiovascular patients, which causes dangerous hyperkalemia or Warfarin antagonism.
- **NutriPath Solution**: Hard negative constraints injected directly into the recommendation pipeline before synthesis. For patients on Warfarin, high-Vitamin-K greens are strictly blocked. For CKD / diuretic patients, high-potassium foods are capped or substituted (e.g., swapping spinach for bottle gourd / lauki, or high-potassium potatoes for ash gourd).

### Decision 2: Multi-Objective SUM Conflict Resolution
- **Problem**: When a user has both Type 2 Diabetes (requiring low GI / controlled carbs) and Chronic Kidney Disease (requiring protein and potassium restriction), naive models generate conflicting or starving diets.
- **NutriPath Solution**: NutriOrion's Severity-Urgency-Modifiability scoring:
  $$S(j) = w_{sev} \cdot \sigma_{sev}(j) + w_{urg} \cdot \sigma_{urg}(j) + w_{mod} \cdot \sigma_{mod}(j)$$
  Prioritizes immediate physiological hazards (e.g., hyperkalemia or acute hypoglycemia) while optimizing long-term glycemic and lipid control.

### Decision 3: Deterministic Nutritional Calculations vs. LLM Estimation (NutriGen Rule)
- **Problem**: LLMs exhibit up to 24.6% Mean Absolute Error (MAE) when estimating calories and macros in free text.
- **NutriPath Solution**: Gemini generates the dish concept and culinary preparation, but all nutrient totals are computed deterministically from the ingredient gram weights cross-referenced against the food composition database (ICMR-NIN & Anuvaad).

### Decision 4: Circular Interactive Workflow (HealthGenie Rule)
- **Problem**: Traditional chat interfaces require users to write lengthy multi-turn prompts ("remove pepper", "I don't like tomatoes"), causing interaction fatigue and high latency.
- **NutriPath Solution**: Visual ingredient chips with 1-tap Include/Exclude actions that immediately trigger local constraint propagation and instant recipe adaptation.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌───────────────────────────────────────────────────────────────────────────┐
│                           NutriPath UI (Compose)                          │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌──────────────────┐ │
│  │   Daily Dashboard     │ │  Interactive Planner  │ │  Health Profile  │ │
│  │ (Macro/Micro Arcs)    │ │ (Include/Exclude KG)  │ │ (Meds, Diseases) │ │
│  └───────────┬───────────┘ └───────────┬───────────┘ └────────┬─────────┘ │
└──────────────┼─────────────────────────┼──────────────────────┼───────────┘
               │                         │                      │
┌──────────────▼─────────────────────────▼──────────────────────▼───────────┐
│                            NutriPath ViewModels                           │
└──────────────────────────────┬────────────────────────────────────────────┘
                               │
┌──────────────────────────────▼────────────────────────────────────────────┐
│                       Core Clinical & Nutrition Engines                   │
│  ┌──────────────────────┐ ┌──────────────────────┐ ┌────────────────────┐ │
│  │ Multi-Objective SUM  │ │ Deterministic Calc   │ │ Rule-Based Safety  │ │
│  │ Prioritizer (Orion)  │ │ Engine (NutriGen)    │ │ Gate & DNI Guard   │ │
│  └───────────┬──────────┘ └───────────┬──────────┘ └──────────┬─────────┘ │
└──────────────┼────────────────────────┼───────────────────────┼───────────┘
               │                        │                       │
┌──────────────▼────────────────────────▼───────────────────────▼───────────┐
│                               Data Layer                                  │
│  ┌───────────────────────┐ ┌───────────────────────┐ ┌──────────────────┐ │
│  │   Room SQLite DB      │ │ Anuvaad Indian Foods  │ │  DailyMed / FDA  │ │
│  │  (Profiles & Plans)   │ │  Repository (1000+)   │ │   DNI Rules DB   │ │
│  └───────────────────────┘ └───────────────────────┘ └──────────────────┘ │
└───────────────────────────────────────────────────────────────────────────┘
```

### Core Data Models

- **`FoodIngredient`**: ID, English name, Regional name (Malayalam/Hindi), category, calories, protein, carbs, fats, fiber, sodium, potassium, calcium, iron, glycemic index, allergen tags, and source attribution (Anuvaad / ICMR-NIN / USDA).
- **`DrugNutrientRule`**: Medication identifier (e.g., Warfarin, Spironolactone, Atorvastatin), restricted nutrient/ingredient (Vitamin K, high potassium, grapefruit), risk severity, and clinical mechanism rationale.
- **`ADIMEIntervention`**:
  - `Assessment`: Patient clinical metrics, target caloric and micronutrient ranges.
  - `Diagnosis`: Prioritized nutrition risk statement (e.g., "Risk of hyperkalemia due to Stage 3 CKD with ACE-inhibitor use").
  - `Intervention`: Concrete directives categorized into `CONTINUE`, `REPLACE`, and `ADD`.
  - `Monitoring`: Key biomarkers and daily intake tracking limits.
- **`InteractiveRecipe`**: Title, meal type, prep time, ingredients (with gram weights and interactive Include/Exclude toggles), cooking instructions, deterministic nutrient breakdown, safety status (`APPROVED`, `MODIFIED`, `WARNING`), and suitability rationale.

### Implementation Phases

1. **Step 1: Clinical Rules & DNI Knowledge Base Expansion**:
   - Implement NutriOrion's Drug-Nutrient Interaction (DNI) safety tables (Warfarin ↔ Vitamin K, K-Sparing Diuretics ↔ Potassium, Statins ↔ Grapefruit, Celiac ↔ Gluten).
   - Implement the Multi-Objective SUM prioritization scoring algorithm for multimorbidity conflict resolution.

2. **Step 2: Anuvaad Ingestion & Indian Food Composition Engine**:
   - Build the high-performance food repository supporting 1,000+ Anuvaad food items and Kerala staples with full micronutrient attributes.
   - Implement CSV parser & importer for live user dataset uploads.

3. **Step 3: Circular Interactive UI & ADIME Clinical Inspector**:
   - Build HealthGenie-style interactive ingredient graph chips (tap to exclude/include with instant adaptation).
   - Build the ADIME Clinical Inspector dialog displaying diagnostic insights and evidence-based rationales.
   - Improve profile readability and contrast adhering to WCAG AAA standards.

4. **Step 4: Verification & Build Validation**:
   - Compile and verify applet integrity via `compile_applet`.
   - Validate Room schema migrations and ensure seamless execution.
