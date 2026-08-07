/**
 * Meal-plan JSON shape validation (the `output_config` json_schema mirrors this).
 */

export const MEAL_TYPES = ["breakfast", "lunch", "dinner", "snack"] as const;
export type MealType = (typeof MEAL_TYPES)[number];

export interface PlanItem {
  food_name: string;
  portion?: string | null;
  serving_count?: number;
  notes?: string | null;
}

export interface PlanMeal {
  meal_type: MealType;
  name: string;
  items: PlanItem[];
}

export interface PlanDay {
  day: number;
  meals: PlanMeal[];
  total_calories_kcal: number;
  protein_g?: number;
  carbs_g?: number;
  fat_g?: number;
}

export interface MealPlanJSON {
  summary: string;
  days: PlanDay[];
}

/** JSON schema handed to the API via output_config — keep in sync with the types above. */
export const PLAN_SCHEMA: Record<string, unknown> = {
  type: "object",
  properties: {
    summary: { type: "string" },
    days: {
      type: "array",
      items: {
        type: "object",
        properties: {
          day: { type: "integer" },
          meals: {
            type: "array",
            items: {
              type: "object",
              properties: {
                meal_type: { type: "string", enum: [...MEAL_TYPES] },
                name: { type: "string" },
                items: {
                  type: "array",
                  items: {
                    type: "object",
                    properties: {
                      food_name: { type: "string" },
                      portion: { type: "string" },
                      serving_count: { type: "number" },
                      notes: { type: "string" },
                    },
                    required: ["food_name"],
                  },
                },
              },
              required: ["meal_type", "name", "items"],
            },
          },
          total_calories_kcal: { type: "number" },
          protein_g: { type: "number" },
          carbs_g: { type: "number" },
          fat_g: { type: "number" },
        },
        required: ["day", "meals", "total_calories_kcal"],
      },
    },
  },
  required: ["summary", "days"],
};

export class PlanValidationError extends Error {}

function isRecord(v: unknown): v is Record<string, unknown> {
  return typeof v === "object" && v !== null && !Array.isArray(v);
}

/** Strict shape check of the parsed Claude JSON. Throws PlanValidationError on any issue. */
export function validatePlan(data: unknown): MealPlanJSON {
  if (!isRecord(data)) throw new PlanValidationError("plan root must be an object");
  if (typeof data.summary !== "string") throw new PlanValidationError("plan.summary must be a string");
  if (!Array.isArray(data.days) || data.days.length === 0) {
    throw new PlanValidationError("plan.days must be a non-empty array");
  }

  const days: PlanDay[] = data.days.map((d, di) => {
    if (!isRecord(d)) throw new PlanValidationError(`days[${di}] must be an object`);
    if (typeof d.day !== "number" || !Number.isInteger(d.day)) {
      throw new PlanValidationError(`days[${di}].day must be an integer`);
    }
    if (!Array.isArray(d.meals) || d.meals.length === 0) {
      throw new PlanValidationError(`days[${di}].meals must be a non-empty array`);
    }
    const meals: PlanMeal[] = d.meals.map((m, mi) => {
      if (!isRecord(m)) throw new PlanValidationError(`days[${di}].meals[${mi}] must be an object`);
      if (!MEAL_TYPES.includes(m.meal_type as MealType)) {
        throw new PlanValidationError(`days[${di}].meals[${mi}].meal_type invalid`);
      }
      if (typeof m.name !== "string") {
        throw new PlanValidationError(`days[${di}].meals[${mi}].name must be a string`);
      }
      if (!Array.isArray(m.items) || m.items.length === 0) {
        throw new PlanValidationError(`days[${di}].meals[${mi}].items must be a non-empty array`);
      }
      const items: PlanItem[] = m.items.map((it) => {
        if (!isRecord(it)) throw new PlanValidationError("plan item must be an object");
        if (typeof it.food_name !== "string" || it.food_name.trim() === "") {
          throw new PlanValidationError("plan item food_name must be a non-empty string");
        }
        return {
          food_name: it.food_name,
          portion: typeof it.portion === "string" ? it.portion : null,
          serving_count: typeof it.serving_count === "number" ? it.serving_count : 1,
          notes: typeof it.notes === "string" ? it.notes : null,
        };
      });
      return { meal_type: m.meal_type as MealType, name: m.name, items };
    });
    return {
      day: d.day,
      meals,
      total_calories_kcal: typeof d.total_calories_kcal === "number" ? d.total_calories_kcal : 0,
      protein_g: typeof d.protein_g === "number" ? d.protein_g : undefined,
      carbs_g: typeof d.carbs_g === "number" ? d.carbs_g : undefined,
      fat_g: typeof d.fat_g === "number" ? d.fat_g : undefined,
    };
  });

  return { summary: data.summary, days };
}

/** Flatten all plan items (for the safety gate + DB inserts). */
export function flattenItems(plan: MealPlanJSON): PlanItem[] {
  return plan.days.flatMap((d) => d.meals.flatMap((m) => m.items));
}
