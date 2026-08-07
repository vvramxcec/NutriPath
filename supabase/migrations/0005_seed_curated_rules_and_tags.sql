-- 0005_seed_curated_rules_and_tags.sql
-- NutriPath — curated per-food overrides, dietary restrictions, and food tags.
-- These cover rules the INDB data can't express (purines, goitrogens, gluten)
-- and give the restriction filters something to match against.
--
-- NOTE: food tags are a curated subset of common foods. Full tag coverage of all
-- 1,014 foods is a follow-up curation task (not blocking MVP).

-- ---------------------------------------------------------------------------
-- 1. condition_food_rules — curated per-food overrides
-- ---------------------------------------------------------------------------

-- Gout: avoid shellfish (high-purine) — ACR guidance
insert into condition_food_rules (condition_id, food_item_id, suitability, reason)
select c.id, f.id, 'avoid', 'High-purine shellfish — ACR gout guidance limits shellfish intake.'
from conditions c, food_items f
where c.slug = 'gout' and f.food_code in ('BFP230', 'OSR027')
on conflict (condition_id, food_item_id) do update
    set suitability = excluded.suitability, reason = excluded.reason;

-- Hypothyroidism: limit raw goitrogenic vegetables — ATA guidance
insert into condition_food_rules (condition_id, food_item_id, suitability, reason)
select c.id, f.id, 'limit', 'Raw cruciferous vegetable — ATA advises limiting large amounts (cooking reduces goitrogens).'
from conditions c, food_items f
where c.slug = 'hypothyroidism' and f.food_code in ('ASC264', 'ASC182', 'BFP030')
on conflict (condition_id, food_item_id) do update
    set suitability = excluded.suitability, reason = excluded.reason;

-- Celiac: avoid gluten staples (belt-and-suspenders on top of food_exclusions tags)
insert into condition_food_rules (condition_id, food_item_id, suitability, reason)
select c.id, f.id, 'avoid', 'Contains gluten (wheat) — strictly avoided in celiac disease.'
from conditions c, food_items f
where c.slug = 'celiac' and f.food_code in ('ASC096', 'ASC097', 'ASC107', 'ASC142', 'ASC143')
on conflict (condition_id, food_item_id) do update
    set suitability = excluded.suitability, reason = excluded.reason;

-- Arthritis: encourage anti-inflammatory foods (fatty fish, antioxidant-rich)
insert into condition_food_rules (condition_id, food_item_id, suitability, reason)
select c.id, f.id, 'allowed', 'Encourage — anti-inflammatory choice (ACR dietary guidance for arthritis).'
from conditions c, food_items f
where c.slug = 'arthritis' and f.food_code in
    ('ASC246', 'ASC251', 'BFP223', 'OSR064', 'OSR089')
on conflict (condition_id, food_item_id) do update
    set suitability = excluded.suitability, reason = excluded.reason;

-- ---------------------------------------------------------------------------
-- 2. dietary_restrictions — tag filters for the restriction selector
-- ---------------------------------------------------------------------------
insert into public.dietary_restrictions (slug, name, description, forbidden_tags, required_tags) values
    ('vegetarian',  'Vegetarian',        'No meat, fish, poultry or eggs.',             array['non_veg', 'egg'],  array[]::text[]),
    ('vegan',       'Vegan',             'No animal products at all.',                  array['non_veg', 'egg', 'dairy'], array[]::text[]),
    ('gluten-free', 'Gluten-free',       'Avoids wheat, barley, rye and their flours.', array['contains_gluten'],  array[]::text[]),
    ('dairy-free',  'Dairy-free',        'No milk, cheese, paneer, curd, ghee or butter.', array['dairy'],       array[]::text[]),
    ('nut-free',    'Nut-free',          'No peanuts, cashews, almonds, walnuts.',      array['nuts'],            array[]::text[]),
    ('low-sodium',  'Low-sodium',        'Prefer foods low in added salt.',             array['high_sodium'],     array[]::text[])
on conflict (slug) do nothing;

-- ---------------------------------------------------------------------------
-- 3. food tags — curated subset of common foods
-- ---------------------------------------------------------------------------

-- non_veg: meat / fish / shellfish dishes
update food_items set tags = array_append(tags, 'non_veg')
where food_code = any(array[
    'ASC028','ASC031','ASC035','ASC043','ASC044','ASC064','ASC070','ASC081','ASC087','ASC090',
    'ASC104','ASC122','ASC135','ASC140','ASC227','ASC228','ASC229','ASC230','ASC231','ASC232',
    'ASC234','ASC236','ASC237','ASC238','ASC239','ASC240','ASC241','ASC242','ASC243','ASC244',
    'ASC245','ASC246','ASC247','ASC248','ASC249','ASC250','ASC251','ASC252',
    'BFP090','BFP141','BFP142','BFP155','BFP157','BFP158','BFP161','BFP194','BFP196','BFP197',
    'BFP198','BFP199','BFP201','BFP212','BFP214','BFP216','BFP217','BFP218','BFP220','BFP221',
    'BFP222','BFP223','BFP226','BFP229','BFP230','BFP231','BFP232','BFP233','BFP234',
    'OSR027','OSR029','OSR062','OSR063','OSR064','OSR065','OSR066','OSR067','OSR068','OSR073',
    'OSR086','OSR095','OSR128'
  ]) and not ('non_veg' = any(tags));

-- shellfish: subset of non_veg, used for the gout override
update food_items set tags = array_append(tags, 'shellfish')
where food_code = any(array['BFP230', 'OSR027']) and not ('shellfish' = any(tags));

-- egg: contains egg
update food_items set tags = array_append(tags, 'egg')
where food_code = any(array[
    'ASC020','ASC024','ASC037','ASC056','ASC057','ASC058','ASC059','ASC060','ASC061','ASC062',
    'ASC063','ASC089','ASC238','ASC256','ASC296','ASC357','ASC380',
    'BFP052','BFP054','BFP055','BFP056','BFP057','BFP058','BFP165','BFP240','BFP299','BFP428',
    'OSR069','OSR070','OSR071','OSR072'
  ]) and not ('egg' = any(tags));

-- dairy: milk / cheese / paneer / curd / ghee / butter / ice cream / kheer
update food_items set tags = array_append(tags, 'dairy')
where food_code = any(array[
    'ASC014','ASC015','ASC016','ASC017','ASC018','ASC019','ASC021','ASC022','ASC023','ASC026',
    'ASC027','ASC039','ASC040','ASC042','ASC051','ASC074','ASC105','ASC118','ASC126','ASC133',
    'ASC149','ASC191','ASC195','ASC215','ASC221','ASC222','ASC223','ASC224','ASC225','ASC226',
    'ASC269','ASC270','ASC271','ASC272','ASC273','ASC274','ASC275','ASC276','ASC277','ASC278',
    'ASC279','ASC282','ASC283','ASC285','ASC286','ASC287','ASC288','ASC289','ASC290','ASC291',
    'ASC292','ASC293','ASC304','ASC305','ASC307','ASC308','ASC321','ASC324','ASC326','ASC335',
    'ASC336','ASC337','ASC338','ASC339','ASC340','ASC341','ASC342','ASC343','ASC344','ASC346',
    'ASC348','ASC350','ASC376','ASC409','ASC413',
    'BFP011','BFP013','BFP015','BFP019','BFP075','BFP087','BFP091','BFP125','BFP286','BFP294',
    'BFP303','BFP313','BFP316','BFP318','BFP320','BFP321','BFP322','BFP325','BFP326','BFP327',
    'BFP328','BFP329','BFP334','BFP336','BFP338','BFP346','BFP347','BFP348','BFP350','BFP351',
    'BFP353','BFP354','BFP386','BFP392','BFP393','BFP396','BFP397','BFP401','BFP403','BFP404',
    'BFP406','BFP407','BFP452','BFP470','BFP518','BFP534',
    'OSR010','OSR024','OSR025','OSR041','OSR088','OSR089','OSR090','OSR091','OSR121','OSR126'
  ]) and not ('dairy' = any(tags));

-- nuts: peanut / cashew / almond / walnut / groundnut
update food_items set tags = array_append(tags, 'nuts')
where food_code = any(array[
    'ASC029','ASC119','ASC270','ASC339','ASC368','ASC382','ASC385','ASC439','ASC445','ASC459',
    'BFP087','BFP320','BFP446','BFP458','BFP461','BFP502','BFP517','BFP553','BFP571',
    'OSR040','OSR126'
  ]) and not ('nuts' = any(tags));

-- contains_gluten: wheat-based breads, parathas, pooris, pasta, noodles, cakes, biscuits, pizza
update food_items set tags = array_append(tags, 'contains_gluten')
where food_code = any(array[
    'ASC023','ASC024','ASC025','ASC026','ASC027','ASC028','ASC029','ASC030','ASC031','ASC032',
    'ASC033','ASC034','ASC035','ASC036','ASC037','ASC038','ASC039','ASC040','ASC041','ASC042',
    'ASC043','ASC044','ASC045','ASC046','ASC047','ASC048','ASC063','ASC064','ASC065','ASC066',
    'ASC067','ASC096','ASC097','ASC098','ASC099','ASC100','ASC101','ASC102','ASC103','ASC104',
    'ASC105','ASC106','ASC107','ASC108','ASC109','ASC110','ASC111','ASC112','ASC133','ASC134',
    'ASC135','ASC136','ASC137','ASC138','ASC139','ASC140','ASC141','ASC142','ASC143','ASC148',
    'ASC358','ASC372','ASC375','ASC376','ASC383','ASC407','ASC408','ASC417','ASC418','ASC419',
    'ASC420','ASC421','ASC422','ASC423','ASC424','ASC425','ASC426','ASC427','ASC429','ASC430',
    'ASC431','ASC432','ASC433','ASC439','ASC440','ASC441','ASC442','ASC443','ASC444','ASC445',
    'ASC446','ASC447','ASC448','ASC449','ASC450','ASC451','ASC452','ASC453','ASC454','ASC455',
    'BFP036','BFP039','BFP040','BFP041','BFP042','BFP122','BFP124','BFP125','BFP147','BFP154',
    'BFP155','BFP157','BFP158','BFP161','BFP162','BFP165','BFP518','BFP534',
    'OSR014','OSR102','OSR104','OSR108','OSR120','OSR123','OSR152'
  ]) and not ('contains_gluten' = any(tags));

-- high_sugar: desserts, sweets, sweet drinks, jams, squashes
update food_items set tags = array_append(tags, 'high_sugar')
where food_code = any(array[
    'ASC005','ASC006','ASC007','ASC008','ASC009','ASC011','ASC012','ASC015','ASC016','ASC017',
    'ASC018','ASC019','ASC020','ASC021','ASC038','ASC066','ASC125','ASC263','ASC280','ASC281',
    'ASC282','ASC283','ASC285','ASC286','ASC287','ASC288','ASC289','ASC290','ASC291','ASC292',
    'ASC293','ASC294','ASC295','ASC296','ASC297','ASC298','ASC299','ASC300','ASC301','ASC302',
    'ASC304','ASC305','ASC307','ASC308','ASC309','ASC310','ASC311','ASC312','ASC313','ASC314',
    'ASC315','ASC316','ASC317','ASC318','ASC319','ASC320','ASC321','ASC322','ASC323','ASC324',
    'ASC325','ASC326','ASC327','ASC328','ASC329','ASC330','ASC331','ASC332','ASC333','ASC334',
    'ASC335','ASC336','ASC337','ASC338','ASC339','ASC340','ASC341','ASC342','ASC343','ASC344',
    'ASC345','ASC346','ASC347','ASC348','ASC349','ASC350','ASC395','ASC396','ASC397','ASC398',
    'ASC399','ASC402','ASC403','ASC404','ASC405','ASC463','ASC464','ASC465','ASC466','ASC467',
    'ASC492','ASC493','ASC494','ASC495','ASC496','ASC497','ASC498','ASC499','ASC500','ASC501',
    'ASC502','ASC503','ASC504','ASC505','ASC506',
    'BFP346','BFP347','BFP348','BFP350','BFP351','BFP352','BFP353','BFP354','BFP355','BFP356',
    'BFP358','BFP359','BFP361','BFP362','BFP363','BFP364','BFP365','BFP366','BFP367','BFP368',
    'BFP369','BFP370','BFP371','BFP372','BFP373','BFP379','BFP380','BFP382','BFP386','BFP388',
    'BFP389','BFP390','BFP391','BFP392','BFP393','BFP396','BFP397','BFP401','BFP403','BFP404',
    'BFP406','BFP407','BFP412','BFP413','BFP583','BFP585','BFP589',
    'OSR001','OSR004','OSR005','OSR008','OSR010','OSR011','OSR012','OSR013','OSR014','OSR016',
    'OSR017','OSR018','OSR019','OSR020','OSR021','OSR022','OSR023','OSR024','OSR025','OSR026',
    'OSR031','OSR032','OSR033','OSR034','OSR035','OSR036','OSR037','OSR038','OSR039','OSR040',
    'OSR041','OSR042','OSR045','OSR046','OSR047','OSR048','OSR061','OSR120','OSR121','OSR122',
    'OSR123','OSR124','OSR125','OSR126','OSR127'
  ]) and not ('high_sugar' = any(tags));

-- high_sodium: pickles, masalas, salty sauces/condiments, savoury snacks
update food_items set tags = array_append(tags, 'high_sodium')
where food_code = any(array[
    'ASC074','ASC266','ASC268','ASC362','ASC364','ASC365','ASC514',
    'BFP001','BFP002','BFP003','BFP004','BFP005','BFP095','BFP096','BFP097','BFP163','BFP415',
    'BFP416','BFP599','BFP601','BFP602',
    'OSR049','OSR050','OSR051','OSR052','OSR053','OSR054','OSR055','OSR056','OSR057','OSR058',
    'OSR059','OSR060','OSR080','OSR097','OSR113','OSR119','OSR147','OSR148'
  ]) and not ('high_sodium' = any(tags));
