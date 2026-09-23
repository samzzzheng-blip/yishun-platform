
-- Data row omitted from source archive; restore data from protected backup.

SELECT id, name, user_id FROM app_category
WHERE name='直购物品' AND user_id=0 AND deleted=0 ORDER BY id;

-- Historical review only: verify these rows before a separate migration.
-- Paid order + category owner + generated category name establish the association.
SELECT DISTINCT c.id, c.user_id, c.category_id, c.category_name
FROM app_collection c
JOIN app_category cat ON cat.id=c.category_id AND cat.user_id=c.user_id
JOIN app_ykj_order o ON o.user_id=c.user_id AND o.status=1 AND o.deleted=0
WHERE c.deleted=0 AND cat.deleted=0
  AND cat.name=CONCAT('直购',o.ykj_id)
  AND c.category_name=cat.name;
