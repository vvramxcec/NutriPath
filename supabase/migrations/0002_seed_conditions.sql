-- 0002_seed_conditions.sql
-- NutriPath — condition catalog. Slugs match tools/rules/nutrient_limits.py keys.

insert into public.conditions (slug, name, description) values
    ('type2_diabetes', 'Type 2 Diabetes', 'Body has trouble regulating blood sugar. Meal plans favor low added sugar, controlled carbs, and high fiber.'),
    ('hypertension',   'High Blood Pressure', 'Elevated blood pressure. DASH-style eating: low sodium, low saturated fat, high potassium.'),
    ('dyslipidemia',   'High Cholesterol', 'Elevated LDL or triglycerides. Limit saturated and trans fats; favor soluble fiber.'),
    ('ckd',            'Chronic Kidney Disease', 'Kidneys filter less well. Limits potassium, phosphorus, sodium, and protein depend on stage.'),
    ('gout',           'Gout', 'Uric acid build-up in joints. Avoid high-purine foods and excess fructose.'),
    ('arthritis',      'Arthritis', 'Joint inflammation. Favor anti-inflammatory foods; limit pro-inflammatory fats.'),
    ('hypothyroidism', 'Hypothyroidism', 'Underactive thyroid. Avoid excess iodine and large amounts of raw goitrogenic vegetables.'),
    ('nafld',          'Fatty Liver', 'Fat accumulates in the liver. Limit added sugar and saturated fat.'),
    ('obesity',        'Obesity / Weight Management', 'Weight management focus: calorie deficit per individual needs, high-fiber satiety foods.'),
    ('celiac',         'Celiac Disease', 'Autoimmune reaction to gluten. Strictly avoid wheat, barley, and rye.')
on conflict (slug) do nothing;
