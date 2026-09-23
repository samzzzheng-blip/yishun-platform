-- Deploy before the new purchase handler. Idempotent initialization only;
-- Data row omitted from source archive; restore data from protected backup.


-- Review historical rows separately: the former purchase handler overwrote their original categories.
-- Do not guess original product types from titles or change stock to make them appear.
SELECT c.id,c.name,c.user_id,c.category_id,c.category_name,c.stock,c.real_stock,
       cat.deleted AS category_deleted
FROM app_collection c LEFT JOIN app_category cat ON cat.id=c.category_id
WHERE c.deleted=0 AND (c.category_name='直购物品' OR c.category_name REGEXP '^直购[0-9]+$');
