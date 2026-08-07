"""
NutriPath - Clinical Rule Tables (v1)
Sourced from public guideline documents. NOT clinically reviewed by a
professional - treat as a starting dataset, flag in-app as unvalidated
until checked by a nutritionist/clinician.

Units: all limits are PER DAY unless noted. Food-level checks (per 100g
or per serving) get compared against these totals during validation.
"""

# ---------------------------------------------------------------------
# THRESHOLD-BASED RULES
# condition_id -> { nutrient: {max: X, min: Y, unit: str, note: str} }
# ---------------------------------------------------------------------

NUTRIENT_LIMITS = {

    "type2_diabetes": {
        "sugar_g":        {"max": 25,   "unit": "g",  "note": "Added sugar limit - ADA"},
        "carbs_g":        {"max": 200,  "unit": "g",  "note": "Total carb budget, adjust per calorie needs - ADA"},
        "fiber_g":        {"min": 25,   "unit": "g",  "note": "Encourage - slows glucose absorption"},
        "source": "ADA Standards of Care in Diabetes"
    },

    "hypertension": {
        "sodium_mg":      {"max": 1500, "unit": "mg", "note": "DASH diet target - AHA"},
        "sat_fat_g":      {"max": 13,   "unit": "g",  "note": "AHA saturated fat limit (~6% of 2000kcal diet)"},
        "potassium_mg":   {"min": 3500, "unit": "mg", "note": "Encourage - AHA"},
        "source": "AHA / DASH Diet Guidelines"
    },

    "dyslipidemia": {
        "sat_fat_g":      {"max": 13,   "unit": "g",  "note": "AHA lipid guideline"},
        "trans_fat_g":    {"max": 0,    "unit": "g",  "note": "AHA - avoid entirely"},
        "fiber_g":        {"min": 25,   "unit": "g",  "note": "Encourage - soluble fiber lowers LDL"},
        "source": "AHA Cholesterol Guidelines"
    },

    "ckd": {
        "potassium_mg":   {"max": 2000, "unit": "mg", "note": "KDIGO - varies by CKD stage, this is moderate-stage default"},
        "phosphorus_mg":  {"max": 800,  "unit": "mg", "note": "KDIGO"},
        "sodium_mg":      {"max": 2000, "unit": "mg", "note": "KDIGO"},
        "protein_g":      {"max": 60,   "unit": "g",  "note": "KDIGO - non-dialysis default, ~0.8g/kg/day for 75kg adult"},
        "source": "KDIGO Clinical Practice Guideline for Nutrition in CKD"
    },

    "gout": {
        "purine_mg":      {"max": 400,  "unit": "mg", "note": "ACR - limit high-purine foods (organ meat, certain seafood)"},
        "sugar_g":        {"max": 25,   "unit": "g",  "note": "ACR - limit fructose, linked to uric acid"},
        "source": "ACR Guideline for Management of Gout"
    },

    "arthritis": {
        "omega3_g":       {"min": 2,    "unit": "g",  "note": "Encourage - anti-inflammatory, ACR dietary guidance"},
        "sat_fat_g":      {"max": 15,   "unit": "g",  "note": "Limit pro-inflammatory fats"},
        "source": "ACR Dietary Recommendations for Inflammatory Arthritis"
    },

    "hypothyroidism": {
        "iodine_mcg":     {"max": 500,  "unit": "mcg","note": "ATA - avoid excess, esp. if on levothyroxine"},
        "goitrogen_flag": {"max": None, "unit": "flag","note": "Flag raw cruciferous veg in large amounts - ATA"},
        "source": "American Thyroid Association Guidelines"
    },

    "nafld": {
        "sugar_g":        {"max": 25,   "unit": "g",  "note": "AASLD - limit fructose/added sugar"},
        "sat_fat_g":      {"max": 13,   "unit": "g",  "note": "AASLD"},
        "source": "AASLD Practice Guidance for NAFLD"
    },

    "obesity": {
        "calories_kcal":  {"max": None, "unit": "kcal","note": "Set per individual TDEE minus deficit - not a fixed default"},
        "fiber_g":        {"min": 25,   "unit": "g",  "note": "Encourage - satiety"},
        "source": "General calorie-density + satiety guidance"
    },

}

# ---------------------------------------------------------------------
# EXCLUSION-BASED RULES (binary - no threshold, food tag must be absent)
# ---------------------------------------------------------------------

FOOD_EXCLUSIONS = {

    "celiac": {
        "excluded_tags": ["contains_gluten", "wheat", "barley", "rye"],
        "source": "Celiac Disease Foundation / gluten-free dietary standard"
    },

}

# ---------------------------------------------------------------------
# Helper: resolve limits across multiple active conditions for one user
# Takes the MOST RESTRICTIVE value per nutrient across all conditions.
# ---------------------------------------------------------------------

def resolve_user_limits(condition_ids):
    """
    condition_ids: list of condition keys, e.g. ["type2_diabetes", "ckd"]
    Returns: dict of nutrient -> {max, min} combining strictest limits.
    """
    combined = {}

    for cid in condition_ids:
        rules = NUTRIENT_LIMITS.get(cid, {})
        for nutrient, limit in rules.items():
            if nutrient == "source":
                continue
            if nutrient not in combined:
                combined[nutrient] = {"max": limit.get("max"), "min": limit.get("min")}
            else:
                # take stricter max (lower wins), stricter min (higher wins)
                existing = combined[nutrient]
                new_max = limit.get("max")
                new_min = limit.get("min")
                if new_max is not None:
                    existing["max"] = new_max if existing["max"] is None else min(existing["max"], new_max)
                if new_min is not None:
                    existing["min"] = new_min if existing["min"] is None else max(existing["min"], new_min)

    return combined


def get_excluded_tags(condition_ids):
    """Union of all excluded food tags across a user's conditions."""
    tags = set()
    for cid in condition_ids:
        rules = FOOD_EXCLUSIONS.get(cid, {})
        tags.update(rules.get("excluded_tags", []))
    return list(tags)


if __name__ == "__main__":
    # quick sanity check
    test_user_conditions = ["type2_diabetes", "ckd","nafld"]
    print("Combined limits for", test_user_conditions, ":")
    for nutrient, vals in resolve_user_limits(test_user_conditions).items():
        print(f"  {nutrient}: {vals}")