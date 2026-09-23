-- Production IDs verified 2026-09-18. Back up affected rows before running.
-- Keep user-created copy names and all balances/quantities unchanged.
CREATE TEMPORARY TABLE category_migration_assert (ok INT CHECK (ok = 1));
START TRANSACTION;
SELECT id FROM app_category WHERE id IN (73,76) FOR UPDATE;
-- Data row omitted from source archive; restore data from protected backup.

SET @before_count = (SELECT COUNT(*) FROM app_collection WHERE category_id IN (73,76));
SET @before_stock = (SELECT COALESCE(SUM(stock),0) FROM app_collection WHERE category_id IN (73,76));
SET @before_real_stock = (SELECT COALESCE(SUM(real_stock),0) FROM app_collection WHERE category_id IN (73,76));

UPDATE app_category SET name='评级徽章/纪念章',update_time=update_time WHERE id=76;
UPDATE app_collection SET category_id=76,category_name='评级徽章/纪念章',update_time=update_time WHERE category_id IN (73,76);
UPDATE app_category SET copy_id=76,update_time=update_time WHERE copy_id=73;
UPDATE app_buy_order SET category_id=76,update_time=update_time WHERE category_id=73;
UPDATE app_sell_order SET category_id=76,update_time=update_time WHERE category_id=73;
UPDATE app_deal_order SET category_id=76,update_time=update_time WHERE category_id=73;
UPDATE app_collection_record SET category_id=76,update_time=update_time WHERE category_id=73;
UPDATE app_category SET deleted=1,update_time=update_time WHERE id=73;
-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

COMMIT;
DROP TEMPORARY TABLE category_migration_assert;
SELECT id,name FROM app_category WHERE user_id=0 AND deleted=0 ORDER BY id;
