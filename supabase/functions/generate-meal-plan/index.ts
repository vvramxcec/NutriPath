/**
 * NutriPath — generate-meal-plan edge function.
 *
 * Flow: load profile + conditions + rules (service role) → resolve strictest
 * limits → score every food → build prompt → call the LLM (Gemini by default,
 * Claude if LLM_PROVIDER=anthropic) → validate + assertMealSafe → persist
 * meal_plans / meal_plan_items.
 *
 * The rules layer ENFORCES (avoid/limit/pool); the LLM only proposes within the
 * allowed pool. Extensible: new conditions/limits = DB rows, no code change.
 */
import { corsHeaders, corsResponse } from "./_shared/cors.ts";
import { getJson, postJson, patchJson, REST, enc } from "./_shared/db.ts";
import {
  resolveUserLimits,
  buildExcludedTags,
  scoreAllFoods,
  assertMealSafe,
  normalizedName,
  type FoodItem,
  type NutrientLimit,
  type Condition,
} from "./_shared/rules.ts";
import { buildPrompt } from "./_shared/prompt.ts";
import { callLlm } from "./_shared/llm.ts";
import {
  validatePlan,
  flattenItems,
  PLAN_SCHEMA,
  type MealPlanJSON,
} from "./_shared/validation.ts";

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

interface Profile {
  id: string;
  display_name: string | null;
  age: number | null;
  sex: string | null;
  height_cm: number | null;
  weight_kg: number | null;
  activity_level: string | null;
}

const json = (body: unknown, status = 200): Response =>
  new Response(JSON.stringify(body), {
    status,
    headers: { ...corsHeaders, "content-type": "application/json" },
  });

// --- calorie math (Mifflin-St Jeor + activity factor) ----------------------
function bmr(p: Profile): number | null {
  if (!p.weight_kg || !p.height_cm || !p.age) return null;
  const base = 10 * p.weight_kg + 6.25 * p.height_cm - 5 * p.age;
  if (p.sex === "male") return base + 5;
  if (p.sex === "female") return base - 161;
  return base - 78;
}
function activityFactor(a: string | null): number {
  switch (a) {
    case "light": return 1.375;
    case "moderate": return 1.55;
    case "active": return 1.725;
    default: return 1.2;
  }
}
function calorieTarget(p: Profile, hasObesity: boolean): number {
  const b = bmr(p);
  if (b === null) return 2000;
  const tdee = b * activityFactor(p.activity_level);
  return Math.round(hasObesity ? tdee - 500 : tdee);
}
function bmi(p: Profile): number | null {
  if (!p.weight_kg || !p.height_cm) return null;
  const m = p.height_cm / 100;
  return p.weight_kg / (m * m);
}

/** Load ALL foods, paginating past the API row cap. */
async function loadAllFoods(): Promise<FoodItem[]> {
  const pageSize = 1000;
  let offset = 0;
  const all: FoodItem[] = [];
  for (;;) {
    const rows = await getJson<FoodItem[]>(
      REST("food_items", `select=*&order=food_code.asc&limit=${pageSize}&offset=${offset}`),
    );
    all.push(...rows);
    if (rows.length < pageSize) break;
    offset += pageSize;
  }
  return all;
}

const nameToFood = (foods: FoodItem[]): Map<string, FoodItem> =>
  new Map(foods.map((f) => [normalizedName(f.name), f]));

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return json({ error: "method not allowed" }, 405);

  let body: Record<string, unknown>;
  try {
    body = await req.json();
  } catch {
    return json({ error: "invalid JSON body" }, 400);
  }

  const profileId = body.profileId;
  if (typeof profileId !== "string" || !UUID_RE.test(profileId)) {
    return json({ error: "profileId must be a UUID" }, 400);
  }

  let planId: string | undefined =
    typeof body.planId === "string" && UUID_RE.test(body.planId) ? body.planId : undefined;
  if (body.planId !== undefined && planId === undefined) {
    return json({ error: "planId must be a UUID" }, 400);
  }

  const days = typeof body.days === "number" && Number.isInteger(body.days)
    ? Math.min(7, Math.max(1, body.days))
    : 3;

  // Ensure a meal_plans row exists (client may have created an optimistic one).
  if (!planId) {
    planId = crypto.randomUUID();
    await postJson(REST("meal_plans", "select=id"), {
      id: planId,
      profile_id: profileId,
      status: "pending",
    });
  }

  try {
    // --- 1. load profile ---------------------------------------------------
    const profiles = await getJson<Profile[]>(REST("profiles", `id=eq.${enc(profileId)}&select=*`));
    const profile = profiles[0];
    if (!profile) throw new Error("profile not found");

    // --- 2. conditions ------------------------------------------------------
    const links = await getJson<{ condition_id: string }[]>(
      REST("profile_conditions", `profile_id=eq.${enc(profileId)}&select=condition_id`),
    );
    const conditionIds = links.map((l) => l.condition_id);
    let conditions: Condition[] = [];
    if (conditionIds.length > 0) {
      conditions = await getJson<Condition[]>(
        REST("conditions", `id=in.(${conditionIds.map(enc).join(",")})&select=*`),
      );
    }

    // --- 3. rules (thresholds + exclusions) --------------------------------
    let limits: NutrientLimit[] = [];
    let exclusions: { excluded_tag: string }[] = [];
    if (conditionIds.length > 0) {
      const inList = `condition_id=in.(${conditionIds.map(enc).join(",")})`;
      limits = await getJson<NutrientLimit[]>(REST("nutrient_limits", `${inList}&select=*`));
      exclusions = await getJson<{ excluded_tag: string }[]>(REST("food_exclusions", `${inList}&select=excluded_tag`));
    }

    // --- 4. dietary restrictions -------------------------------------------
    const restrictionLinks = await getJson<{ restriction_id: string }[]>(
      REST("profile_restrictions", `profile_id=eq.${enc(profileId)}&select=restriction_id`),
    );
    let restrictions: { slug: string; name: string; forbidden_tags: string[] }[] = [];
    if (restrictionLinks.length > 0) {
      const ridList = restrictionLinks.map((l) => l.restriction_id).join(",");
      restrictions = await getJson<{ slug: string; name: string; forbidden_tags: string[] }[]>(
        REST("dietary_restrictions", `id=in.(${ridList})&select=slug,name,forbidden_tags`),
      );
    }

    const resolved = resolveUserLimits(limits, conditions);
    const excludedTags = buildExcludedTags(exclusions, restrictions);

    // --- 5. score all foods --------------------------------------------------
    const foods = await loadAllFoods();
    const { avoidSet, limitSet, pool } = scoreAllFoods(foods, resolved, excludedTags);

    // Cap the prompt-size pool: keep all "encourage" foods, then fill up to
    // MAX_POOL with "ok" foods. Keeps the LLM input under ~20K tokens.
    const MAX_POOL = 200;
    let promptPool = pool;
    if (pool.length > MAX_POOL) {
      const encouraged = pool.filter((s) => s.verdict === "encourage");
      const okPool = pool.filter((s) => s.verdict === "ok");
      // Deterministic: sort by calorie density so the LLM always sees the
      // same core set, making generation reproducible-ish.
      okPool.sort((a, b) =>
        (a.food.calories_per_serving_kcal ?? 0) - (b.food.calories_per_serving_kcal ?? 0)
      );
      promptPool = [...encouraged, ...okPool.slice(0, MAX_POOL - encouraged.length)];
    }

    // --- 6. prompt + Claude ---------------------------------------------------
    const hasObesity = conditions.some((c) => c.slug === "obesity");
    const target = calorieTarget(profile, hasObesity);
    const system = buildPrompt({
      displayName: profile.display_name,
      age: profile.age,
      sex: profile.sex,
      heightCm: profile.height_cm,
      weightKg: profile.weight_kg,
      activityLevel: profile.activity_level,
      bmi: bmi(profile),
      calorieTarget: target,
      conditionNames: conditions.map((c) => c.name),
      restrictions: restrictions.map((r) => r.name),
      limits: resolved,
      pool: promptPool,
      days,
    });

    // Trim: keys pasted via shell frequently carry a trailing newline, which
    // Anthropic rejects as an invalid x-api-key.
    const llm = await callLlm({
      system,
      userMessage: `Generate a ${days}-day meal plan.`,
      schema: PLAN_SCHEMA,
      maxTokens: 16384,
    });

    // --- 7. validate + safety gate --------------------------------------------
    let plan: MealPlanJSON;
    try {
      plan = validatePlan(JSON.parse(llm.text));
    } catch {
      throw new Error(
        `LLM plan JSON was invalid (finish_reason: ${llm.stopReason}, ` +
        `input_tokens: ${llm.inputTokens}, output_tokens: ${llm.outputTokens}). ` +
        `Raw start: ${llm.text.slice(0, 600)}`,
      );
    }
    const items = flattenItems(plan);
    const safety = assertMealSafe(items, avoidSet, limitSet);
    if (!safety.safe) {
      throw new Error(
        `safety gate blocked: plan references foods not in the allowed pool: ${safety.violations.join(", ")}`,
      );
    }

    // --- 8. persist ------------------------------------------------------------
    const foodByName = nameToFood(foods);
    const itemRows = plan.days.flatMap((d) =>
      d.meals.flatMap((m) =>
        m.items.map((it) => {
          const food = foodByName.get(normalizedName(it.food_name));
          return {
            meal_plan_id: planId,
            day: d.day,
            meal_type: m.meal_type,
            food_name: it.food_name,
            portion: it.portion ?? null,
            serving_count: it.serving_count ?? 1,
            notes: it.notes ?? null,
            food_item_id: food?.id ?? null,
            kcal_per_serving: food?.calories_per_serving_kcal ?? null,
            protein_g_per_serving: food?.protein_g ?? null,
            carbs_g_per_serving: food?.carbs_g ?? null,
            fat_g_per_serving: food?.fat_g ?? null,
          };
        })
      )
    );
    await postJson(REST("meal_plan_items"), itemRows);

    const totalKcal = plan.days.reduce((s, d) => s + (d.total_calories_kcal ?? 0), 0);
    await patchJson(REST("meal_plans", `id=eq.${enc(planId)}`), {
      status: "success",
      total_calories_kcal: totalKcal,
      generated_by: "hybrid",
      model_name: llm.model,
      metadata: {
        summary: plan.summary,
        calorie_target: target,
        days,
        avoid_count: avoidSet.length,
        limit_count: limitSet.length,
        full_pool_count: pool.length,
        prompt_pool_count: promptPool.length,
        warnings: safety.warnings,
      },
      error_message: null,
    });

    return json({
      plan,
      rules: {
        calorieTarget: target,
        conditions: conditions.map((c) => c.slug),
        restrictions: restrictions.map((r) => r.slug),
        limits: resolved,
        excludedTags: [...excludedTags],
        avoidCount: avoidSet.length,
        poolCount: pool.length,
      },
      model: llm.model,
      usage: { input_tokens: llm.inputTokens, output_tokens: llm.outputTokens },
      safety,
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : "unknown error";
    await patchJson(REST("meal_plans", `id=eq.${enc(planId)}`), {
      status: "error",
      error_message: message.slice(0, 1000),
    }).catch(() => undefined);
    console.error("generate-meal-plan failed:", message);
    return json({ error: message }, 500);
  }
});
