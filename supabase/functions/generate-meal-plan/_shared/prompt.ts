/**
 * System-prompt construction for the meal-plan generator.
 */
import { ResolvedLimit, FoodScore } from "./rules.ts";

export interface PromptInput {
  displayName: string | null;
  age: number | null;
  sex: string | null;
  heightCm: number | null;
  weightKg: number | null;
  activityLevel: string | null;
  bmi: number | null;
  calorieTarget: number;
  conditionNames: string[];
  restrictions: string[];
  limits: ResolvedLimit[];
  pool: FoodScore[];
  days: number;
}

function bmiCategory(bmi: number | null): string {
  if (bmi === null) return "unknown";
  if (bmi < 18.5) return "underweight";
  if (bmi < 25) return "healthy";
  if (bmi < 30) return "overweight";
  return "obese";
}

function limitBlock(limits: ResolvedLimit[]): string {
  if (limits.length === 0) return "  None on file.";
  return limits
    .map((r) => {
      const maxTxt = r.max !== null ? `max ${r.max} ${r.unit}/day` : "";
      const minTxt = r.min !== null ? `min ${r.min} ${r.unit}/day` : "";
      const bounds = [maxTxt, minTxt].filter(Boolean).join(", ");
      const sources = [...new Set(r.entries.map((e) => e.source).filter(Boolean))].join("; ");
      return `  - ${r.nutrient} (${bounds})${sources ? " — " + sources : ""}`;
    })
    .join("\n");
}

/** Build the system prompt. Pool foods render one line each (bounded-ish). */
export function buildPrompt(input: PromptInput): string {
  const poolLines = input.pool.map((s) => {
    const f = s.food;
    const kcal = f.calories_per_serving_kcal ?? 0;
    const p = f.protein_g ?? 0;
    const c = f.carbs_g ?? 0;
    const fat = f.fat_g ?? 0;
    const note = s.reasons.length > 0 ? ` [${s.reasons.join(" ")}]` : "";
    return `  - ${f.name} | ${kcal} kcal | P ${p}g C ${c}g F ${fat}g${note}`;
  });

  return `You are a clinical nutritionist creating safe, culturally appropriate meal plans for Indian foods.

You MUST follow these hard rules:
1. Choose foods ONLY from the "Allowed foods pool" below. Never invent or substitute foods not listed.
2. Never include an "avoid" food. If it is not in the allowed pool, it is forbidden.
3. Respect every per-day nutrient limit listed for the user's conditions. A food near or over a daily cap for a nutrient must be used sparingly or balanced with low values of that nutrient.
4. Each day should hit the calorie target closely (within ~10%).
5. For every item, write a short "notes" reason (e.g. "Low sodium, fits the CKD limit").

USER PROFILE
- Name: ${input.displayName ?? "Guest"}
- Age: ${input.age ?? "?"} | Sex: ${input.sex ?? "?"}
- Height: ${input.heightCm ?? "?"} cm | Weight: ${input.weightKg ?? "?"} kg
- BMI: ${input.bmi !== null ? input.bmi.toFixed(1) : "?"} (${bmiCategory(input.bmi)})
- Activity: ${input.activityLevel ?? "?"}
- Calorie target: ${input.calorieTarget} kcal/day
- Conditions: ${input.conditionNames.length ? input.conditionNames.join(", ") : "None"}
- Dietary restrictions: ${input.restrictions.length ? input.restrictions.join(", ") : "None"}

NUTRIENT LIMITS (per day, strictest across the user's conditions):
${limitBlock(input.limits)}

ALLOWED FOODS POOL (per-serving values; these are the ONLY foods you may use):
${poolLines.length ? poolLines.join("\n") : "(empty)"}

Generate a ${input.days}-day meal plan. Use the exact food names from the pool. Every meal must list 1-4 foods with portion, serving_count (default 1), and a notes reason. Provide per-day totals (total_calories_kcal, protein_g, carbs_g, fat_g). Respond ONLY with valid JSON matching the provided schema.`;
}
