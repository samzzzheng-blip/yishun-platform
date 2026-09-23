-- 先备份数据库。仅新增可空列，旧程序回退时保留列即可。
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_ykj_order' AND column_name='fulfillment_type'), 'SELECT 1', 'ALTER TABLE app_ykj_order ADD COLUMN fulfillment_type VARCHAR(16) NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_ykj_order' AND column_name='receiver_name'), 'SELECT 1', 'ALTER TABLE app_ykj_order ADD COLUMN receiver_name VARCHAR(30) NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_ykj_order' AND column_name='receiver_mobile'), 'SELECT 1', 'ALTER TABLE app_ykj_order ADD COLUMN receiver_mobile VARCHAR(30) NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_ykj_order' AND column_name='receiver_area_name'), 'SELECT 1', 'ALTER TABLE app_ykj_order ADD COLUMN receiver_area_name VARCHAR(100) NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_ykj_order' AND column_name='receiver_detail_address'), 'SELECT 1', 'ALTER TABLE app_ykj_order ADD COLUMN receiver_detail_address VARCHAR(200) NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_ykj_order' AND column_name='getback_id'), 'SELECT 1', 'ALTER TABLE app_ykj_order ADD COLUMN getback_id BIGINT NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
