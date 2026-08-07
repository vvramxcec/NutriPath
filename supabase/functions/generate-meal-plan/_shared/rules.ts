/**
 * NutriPath — deterministic safety engine.
 *
 * Generic, data-driven: reads `nutrient_limits` / `food_exclusions` rows loaded
 * from the DB. Adding a new condition or limit is a DB INSERT — no code change.
 * The engine enforces; the LLM only proposes within the allowed pool.
 */

export interface NutrientLimit {
  id: string;
  condition_id: string;
  nutrient: string;
  max_value: number | null;
  min_value: number | null;
  unit: string;
  note: string | null;
  source: string | null;
}

export interface Condition {
  id: string;
  slug: string;
  name: string;
  description: string | null;
}

export interface FoodItem {
  id: string;
  food_code: string;
  name: string;
  calories_per_serving_kcal: number | null;
  protein_g: number | null;
  carbs_g: number | null;
  fat_g: number | null;
  sugar_g: number | null;
  fiber_g: number | null;
  sodium_mg: number | null;
  potassium_mg: number | null;
  phosphorus_mg: number | null;
  calcium_mg: number | null;
  sfa_g: number | null;
  cholesterol_mg: number | null;
  tags: string[];
}

export type Verdict = "avoid" | "limit" | "encourage" | "ok";

export interface FoodScore {
  food: FoodItem;
  verdict: Verdict;
  reasons: string[];
}

export interface ResolvedLimit {
  nutrient: string;
  unit: string;
  max: number | null;
  min: number | null;
  entries: {
    conditionSlug: string;
    conditionName: string;
    note: string | null;
    source: string | null;
  }[];
}

/** nutrient (rule key) -> food_items column. null = no column => not threshold-scorable. */
const NUTRIENT_COLUMN: Partial<Record<string, keyof FoodItem>> = {
  sugar_g: "sugar_g",
  carbs_g: "carbs_g",
  fiber_g: "fiber_g",
  sodium_mg: "sodium_mg",
  potassium_mg: "potassium_mg",
  phosphorus_mg: "phosphorus_mg",
  protein_g: "protein_g",
  sat_fat_g: "sfa_g",
  trans_fat_g: undefined,
  omega3_g: undefined,
  purine_mg: undefined,
  iodine_mcg: undefined,
  calories_kcal: "calories_per_serving_kcal",
};

const NUTRIENT_LABEL: Record<string, string> = {
  sugar_g: "Sugar",
  carbs_g: "Carbs",
  fiber_g: "Fiber",
  sodium_mg: "Sodium",
  potassium_mg: "Potassium",
  phosphorus_mg: "Phosphorus",
  protein_g: "Protein",
  sat_fat_g: "Saturated fat",
  trans_fat_g: "Trans fat",
  omega3_g: "Omega-3",
  purine_mg: "Purines",
  iodine_mcg: "Iodine",
  calories_kcal: "Calories",
};

/** Combine per-condition limits for one nutrient: strictest wins (max = lowest, min = highest). */
export function resolveUserLimits(
  limits: NutrientLimit[],
  conditions: Condition[],
): ResolvedLimit[] {
  const byNutrient = new Map<string, ResolvedLimit>();
  const condById = new Map(conditions.map((c) => [c.id, c]));

  for (const l of limits) {
    const cond = condById.get(l.condition_id);
    if (!cond) continue;
    let r = byNutrient.get(l.nutrient);
    if (!r) {
      r = { nutrient: l.nutrient, unit: l.unit, max: null, min: null, entries: [] };
      byNutrient.set(l.nutrient, r);
    }
    if (l.max_value !== null) {
      r.max = r.max === null ? l.max_value : Math.min(r.max, l.max_value);
    }
    if (l.min_value !== null) {
      r.min = r.min === null ? l.min_value : Math.max(r.min, l.min_value);
    }
    r.entries.push({
      conditionSlug: cond.slug,
      conditionName: cond.name,
      note: l.note,
      source: l.source,
    });
  }
  return [...byNutrient.values()];
}

/** Union of condition exclusion tags and dietary restriction forbidden tags. */
export function buildExcludedTags(
  exclusions: { excluded_tag: string }[],
  restrictions: { forbidden_tags: string[] }[],
): Set<string> {
  const tags = new Set<string>();
  for (const e of exclusions) tags.add(e.excluded_tag);
  for (const r of restrictions) for (const t of r.forbidden_tags ?? []) tags.add(t);
  return tags;
}

const RANK: Record<Verdict, number> = { avoid: 3, limit: 2, encourage: 1, ok: 0 };

function uniqueNames(entries: ResolvedLimit["entries"]): string {
  return [...new Set(entries.map((e) => e.conditionName))].join(", ");
}

/** Score a single food against the resolved limits + excluded tags. */
export function scoreFood(
  food: FoodItem,
  limits: ResolvedLimit[],
  excludedTags: Set<string>,
): FoodScore {
  const reasons: string[] = [];
  let worst: Verdict = "ok";

  const hitTags = (food.tags ?? []).filter((t) => excludedTags.has(t));
  if (hitTags.length > 0) {
    worst = "avoid";
    reasons.push(`Contains ${hitTags.join(", ")} — excluded for your profile.`);
  }

  for (const r of limits) {
    const col = NUTRIENT_COLUMN[r.nutrient];
    if (!col) continue;
    const value = food[col] as number | null;
    if (value === null || value === undefined) continue;

    const label = NUTRIENT_LABEL[r.nutrient] ?? r.nutrient;
    if (r.max !== null && value > r.max) {
      const verdict: Verdict = value / r.max > 2 ? "avoid" : "limit";
      reasons.push(
        `${label} ${value} ${r.unit}/serving — above the ${r.max} ${r.unit}/day limit (${uniqueNames(r.entries)}).`,
      );
      if (RANK[verdict] > RANK[worst]) worst = verdict;
    } else if (r.min !== null && value < r.min) {
      reasons.push(
        `${label} ${value} ${r.unit}/serving — below the ${r.min} ${r.unit}/day target (${uniqueNames(r.entries)}); pair with higher-${label.toLowerCase()} foods.`,
      );
      if (RANK["encourage"] > RANK[worst]) worst = "encourage";
    }
  }

  return { food, verdict: worst, reasons };
}

/** Classify all foods into avoid / limit / allowed pools. */
export function scoreAllFoods(
  foods: FoodItem[],
  limits: ResolvedLimit[],
  excludedTags: Set<string>,
): { scores: FoodScore[]; avoidSet: FoodScore[]; limitSet: FoodScore[]; pool: FoodScore[] } {
  const scores = foods.map((f) => scoreFood(f, limits, excludedTags));
  const avoidSet = scores.filter((s) => s.verdict === "avoid");
  const limitSet = scores.filter((s) => s.verdict === "limit");
  const pool = scores.filter((s) => s.verdict === "ok" || s.verdict === "encourage");
  return { scores, avoidSet, limitSet, pool };
}

export function normalizedName(name: string): string {
  return name.trim().toLowerCase().replace(/\s+/g, " ");
}

/**
 * Post-generation safety gate: every LLM-referenced food must not be in the
 * avoid set (and ideally not in the limit set). The rules layer enforces.
 */
export function assertMealSafe(
  items: { food_name: string }[],
  avoidSet: FoodScore[],
  limitSet: FoodScore[],
): { safe: boolean; violations: string[]; warnings: string[] } {
  const avoidNames = new Set(avoidSet.map((s) => normalizedName(s.food.name)));
  const limitNames = new Set(limitSet.map((s) => normalizedName(s.food.name)));
  const violations: string[] = [];
  const warnings: string[] = [];

  for (const it of items) {
    const key = normalizedName(it.food_name);
    if (avoidNames.has(key)) violations.push(it.food_name);
    else if (limitNames.has(key)) warnings.push(it.food_name);
  }
  return { safe: violations.length === 0, violations, warnings };
}
