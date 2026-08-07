-- 0001_initial_schema.sql
-- NutriPath — core schema. Demo mode: no auth dependency; RLS is permissive
-- (see the RLS note at the bottom — swap to auth.uid() when real auth lands).

create extension if not exists pgcrypto;

-- ---------------------------------------------------------------------------
-- profiles — demo mode: created by the client with a device-generated UUID,
-- no auth.uid() linkage.
-- ---------------------------------------------------------------------------
create table public.profiles (
    id              uuid primary key default gen_random_uuid(),
    display_name    text,
    age             int check (age between 1 and 120),
    sex             text check (sex in ('male', 'female', 'other')),
    height_cm       numeric,
    weight_kg       numeric,
    activity_level  text check (activity_level in ('sedentary', 'light', 'moderate', 'active')),
    created_at      timestamptz not null default now(),
    updated_at      timestamptz not null default now()
);

create or replace function public.set_updated_at()
returns trigger
language plpgsql
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

create trigger profiles_set_updated_at
    before update on public.profiles
    for each row execute function public.set_updated_at();

-- ---------------------------------------------------------------------------
-- conditions — the extensible condition catalog. Seeded in 0002.
-- ---------------------------------------------------------------------------
create table public.conditions (
    id          uuid primary key default gen_random_uuid(),
    slug        text not null unique,
    name        text not null,
    description text,
    is_active   boolean not null default true
);

-- ---------------------------------------------------------------------------
-- food_items — seeded from Anuvaad INDB 2024.11 (0004). Typed per-serving
-- columns are the rule-critical values; the full per-100g row is kept in
-- per_100g jsonb for provenance and future custom-serving math.
-- ---------------------------------------------------------------------------
create table public.food_items (
    id                        uuid primary key default gen_random_uuid(),
    food_code                 text not null unique,          -- INDB code, e.g. ASC001
    name                      text not null,
    source                    text not null default 'asc_manual'
                              check (source in ('asc_manual', 'bfp_manual', 'open_source_recipes')),
    -- per-serving typed columns (rule-critical)
    calories_per_serving_kcal numeric,
    protein_g                 numeric,
    carbs_g                   numeric,
    fat_g                     numeric,
    sugar_g                   numeric,
    fiber_g                   numeric,
    sodium_mg                 numeric,
    potassium_mg              numeric,
    phosphorus_mg             numeric,
    calcium_mg                numeric,
    sfa_g                     numeric,
    cholesterol_mg            numeric,
    iron_mg                   numeric,
    vitc_mg                   numeric,
    folate_ug                 numeric,
    -- provenance + future serving math
    per_100g                  jsonb,
    serving_description       text,                           -- null for MVP (curation task)
    serving_fallback          boolean not null default false, -- true when per-100g used as serving approx
    -- curation
    category                  text,
    tags                      text[] not null default '{}',
    created_at                timestamptz not null default now()
);

create index food_items_name_idx on public.food_items (name);

-- ---------------------------------------------------------------------------
-- nutrient_limits — THE extensible rule table. One row per (condition, nutrient)
-- threshold. The edge function reads these generically; adding a disease/limit
-- is an INSERT, never a code change. Seeded from tools/rules/nutrient_limits.py
-- in 0003.
-- ---------------------------------------------------------------------------
create table public.nutrient_limits (
    id           uuid primary key default gen_random_uuid(),
    condition_id uuid not null references public.conditions (id) on delete cascade,
    nutrient     text not null,          -- sugar_g | sodium_mg | sat_fat_g | potassium_mg | ...
    max_value    numeric,                -- null when only a min applies
    min_value    numeric,                -- null when only a max applies
    unit         text not null,          -- g | mg | ug | kcal | flag
    note         text,                   -- user-facing reason
    source       text,                   -- guideline body (ADA, AHA, KDIGO, ...)
    unique (condition_id, nutrient)
);

create index nutrient_limits_condition_idx on public.nutrient_limits (condition_id);

-- ---------------------------------------------------------------------------
-- food_exclusions — tag-based excludes (e.g. celiac -> wheat/barley/rye).
-- ---------------------------------------------------------------------------
create table public.food_exclusions (
    id           uuid primary key default gen_random_uuid(),
    condition_id uuid not null references public.conditions (id) on delete cascade,
    excluded_tag text not null,
    source       text,
    note         text,
    unique (condition_id, excluded_tag)
);

-- ---------------------------------------------------------------------------
-- condition_food_rules — curated per-food overrides for rules the data can't
-- express (gout purines, hypothyroid goitrogens, celiac gluten grains).
-- ---------------------------------------------------------------------------
create table public.condition_food_rules (
    id           uuid primary key default gen_random_uuid(),
    condition_id uuid not null references public.conditions (id) on delete cascade,
    food_item_id uuid not null references public.food_items (id) on delete cascade,
    suitability  text not null check (suitability in ('allowed', 'limit', 'avoid')),
    reason       text,
    unique (condition_id, food_item_id)
);

-- ---------------------------------------------------------------------------
-- dietary_restrictions — vegetarian/vegan/gluten-free/... with tag filters.
-- ---------------------------------------------------------------------------
create table public.dietary_restrictions (
    id             uuid primary key default gen_random_uuid(),
    slug           text not null unique,
    name           text not null,
    description    text,
    forbidden_tags text[] not null default '{}',
    required_tags  text[] not null default '{}'
);

-- ---------------------------------------------------------------------------
-- profile_conditions / profile_restrictions — a profile's selections.
-- ---------------------------------------------------------------------------
create table public.profile_conditions (
    profile_id   uuid not null references public.profiles (id) on delete cascade,
    condition_id uuid not null references public.conditions (id) on delete cascade,
    severity     text,                          -- null for MVP
    primary key (profile_id, condition_id)
);

create table public.profile_restrictions (
    profile_id     uuid not null references public.profiles (id) on delete cascade,
    restriction_id uuid not null references public.dietary_restrictions (id) on delete cascade,
    primary key (profile_id, restriction_id)
);

-- ---------------------------------------------------------------------------
-- meal_plans / meal_plan_items — generated plans.
-- ---------------------------------------------------------------------------
create table public.meal_plans (
    id                 uuid primary key default gen_random_uuid(),
    profile_id         uuid not null references public.profiles (id) on delete cascade,
    plan_date          date not null default current_date,
    status             text not null default 'pending' check (status in ('pending', 'success', 'error')),
    total_calories_kcal numeric,
    generated_by       text check (generated_by in ('rule', 'hybrid')),
    model_name         text,
    metadata           jsonb,                   -- raw LLM response for debugging
    error_message      text,
    created_at         timestamptz not null default now()
);

create index meal_plans_profile_date_idx on public.meal_plans (profile_id, plan_date);

create table public.meal_plan_items (
    id                    uuid primary key default gen_random_uuid(),
    meal_plan_id          uuid not null references public.meal_plans (id) on delete cascade,
    day                   int not null default 1,
    meal_type             text not null check (meal_type in ('breakfast', 'lunch', 'dinner', 'snack')),
    food_item_id          uuid references public.food_items (id) on delete set null,
    food_name             text not null,
    portion               text,
    serving_count         numeric not null default 1,
    notes                 text,                 -- rule reasons / why this food is ok
    -- per-serving nutrition snapshot (from the matched food at generation time)
    kcal_per_serving      numeric,
    protein_g_per_serving numeric,
    carbs_g_per_serving   numeric,
    fat_g_per_serving     numeric
);

create index meal_plan_items_plan_idx on public.meal_plan_items (meal_plan_id);

-- ---------------------------------------------------------------------------
-- RLS — DEMO MODE.
-- Permissive read/write for anon + authenticated so the client works with the
-- anon key and no login. This is deliberately NOT per-user; the edge function
-- runs with the service-role key which bypasses RLS entirely.
-- When real auth lands: replace `to anon, authenticated` with `to authenticated`
-- using (auth.uid() = profile_id) on profiles and related rows.
-- ---------------------------------------------------------------------------
do $$
declare
    t text;
begin
    foreach t in array array[
        'profiles', 'conditions', 'food_items', 'nutrient_limits', 'food_exclusions',
        'condition_food_rules', 'dietary_restrictions', 'profile_conditions',
        'profile_restrictions', 'meal_plans', 'meal_plan_items'
    ] loop
        execute format('alter table public.%I enable row level security;', t);
        execute format(
            'create policy "demo full access" on public.%I for all to anon, authenticated using (true) with check (true);',
            t
        );
    end loop;
end $$;
