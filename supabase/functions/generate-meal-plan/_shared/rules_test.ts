/**
 * Unit tests for the deterministic rules engine. Run with:  deno test
 */
import {
  resolveUserLimits,
  buildExcludedTags,
  scoreFood,
  scoreAllFoods,
  assertMealSafe,
  type FoodItem,
  type NutrientLimit,
  type Condition,
} from "./rules.ts";
import { assert, assertEquals } from "jsr:@std/assert@1";

const COND_A: Condition = { id: "a", slug: "type2_diabetes", name: "Type 2 Diabetes", description: null };
const COND_B: Condition = { id: "b", slug: "ckd", name: "Chronic Kidney Disease", description: null };

function food(overrides: Partial<FoodItem> = {}): FoodItem {
  return {
    id: "f1",
    food_code: "TST001",
    name: "Test food",
    calories_per_serving_kcal: 100,
    protein_g: 5,
    carbs_g: 20,
    fat_g: 5,
    sugar_g: 5,
    fiber_g: 2,
    sodium_mg: 100,
    potassium_mg: 200,
    phosphorus_mg: 100,
    calcium_mg: 10,
    sfa_g: 2,
    cholesterol_mg: 0,
    tags: [],
    ...overrides,
  };
}

function limit(overrides: Partial<NutrientLimit> = {}): NutrientLimit {
  return {
    id: "l1",
    condition_id: "a",
    nutrient: "sugar_g",
    max_value: 25,
    min_value: null,
    unit: "g",
    note: null,
    source: "ADA",
    ...overrides,
  };
}

Deno.test("resolveUserLimits takes the strictest value per nutrient", () => {
  const limits = [
    limit({ id: "1", condition_id: "a", nutrient: "sugar_g", max_value: 25 }),
    limit({ id: "2", condition_id: "b", nutrient: "sugar_g", max_value: 40 }),
    limit({ id: "3", condition_id: "a", nutrient: "fiber_g", min_value: 25, max_value: null }),
    limit({ id: "4", condition_id: "b", nutrient: "fiber_g", min_value: 30, max_value: null }),
  ];
  const resolved = resolveUserLimits(limits, [COND_A, COND_B]);
  const sugar = resolved.find((r) => r.nutrient === "sugar_g")!;
  const fiber = resolved.find((r) => r.nutrient === "fiber_g")!;
  assertEquals(sugar.max, 25); // lowest max wins
  assertEquals(sugar.entries.length, 2);
  assertEquals(fiber.min, 30); // highest min wins
});

Deno.test("scoreFood: exceed max by >2x is avoid, <=2x is limit", () => {
  const resolved = resolveUserLimits([limit({ max_value: 10, unit: "g" })], [COND_A]);
  const mild = scoreFood(food({ sugar_g: 18 }), resolved, new Set()); // 1.8x
  const severe = scoreFood(food({ sugar_g: 25 }), resolved, new Set()); // 2.5x
  const ok = scoreFood(food({ sugar_g: 8 }), resolved, new Set());
  assertEquals(mild.verdict, "limit");
  assertEquals(severe.verdict, "avoid");
  assertEquals(ok.verdict, "ok");
  assert(mild.reasons[0].includes("10 g/day"), "reason should mention the limit");
});

Deno.test("scoreFood: missing a min is encourage, not penalised", () => {
  const resolved = resolveUserLimits([limit({ nutrient: "fiber_g", max_value: null, min_value: 25, unit: "g" })], [COND_A]);
  const low = scoreFood(food({ fiber_g: 5 }), resolved, new Set());
  assertEquals(low.verdict, "encourage");
  assert(low.reasons[0].includes("25 g/day"), "reason should reference the daily target");
});

Deno.test("scoreFood: excluded tag forces avoid", () => {
  const resolved = resolveUserLimits([limit({ max_value: 10, unit: "g" })], [COND_A]);
  const excluded = new Set(["contains_gluten"]);
  const f = food({ sugar_g: 3, tags: ["contains_gluten"] }); // within sugar limit but tag-excluded
  const scored = scoreFood(f, resolved, excluded);
  assertEquals(scored.verdict, "avoid");
  assert(scored.reasons[0].includes("contains_gluten"));
});

Deno.test("buildExcludedTags unions condition exclusions + restriction forbidden tags", () => {
  const tags = buildExcludedTags(
    [{ excluded_tag: "wheat" }, { excluded_tag: "barley" }],
    [{ forbidden_tags: ["non_veg", "egg"] }, { forbidden_tags: ["wheat"] }],
  );
  assertEquals(tags, new Set(["wheat", "barley", "non_veg", "egg"]));
});

Deno.test("assertMealSafe flags avoid foods as violations and limit foods as warnings", () => {
  const resolved = resolveUserLimits([limit({ max_value: 10, unit: "g" })], [COND_A]);
  const avoid = scoreFood(food({ id: "a1", name: "Sweet drink", sugar_g: 30 }), resolved, new Set());
  const limitFood = scoreFood(food({ id: "a2", name: "Salted snack", sugar_g: 15 }), resolved, new Set());
  const allowed = scoreFood(food({ id: "a3", name: "Dal", sugar_g: 2 }), resolved, new Set());
  assertEquals(avoid.verdict, "avoid");
  assertEquals(limitFood.verdict, "limit");
  assertEquals(allowed.verdict, "ok");

  const { safe, violations, warnings } = assertMealSafe(
    [{ food_name: "Sweet drink" }, { food_name: "Salted snack" }, { food_name: "Dal" }],
    [avoid],
    [limitFood],
  );
  assertEquals(safe, false);
  assertEquals(violations, ["Sweet drink"]);
  assertEquals(warnings, ["Salted snack"]);
});

Deno.test("scoreAllFoods partitions into avoid/limit/pool", () => {
  const resolved = resolveUserLimits([limit({ max_value: 10, unit: "g" })], [COND_A]);
  const foods = [
    food({ id: "1", name: "severe", sugar_g: 30 }),
    food({ id: "2", name: "mild", sugar_g: 15 }),
    food({ id: "3", name: "ok", sugar_g: 5 }),
  ];
  const { avoidSet, limitSet, pool } = scoreAllFoods(foods, resolved, new Set());
  assertEquals(avoidSet.map((s) => s.food.name), ["severe"]);
  assertEquals(limitSet.map((s) => s.food.name), ["mild"]);
  assertEquals(pool.map((s) => s.food.name), ["ok"]);
});
