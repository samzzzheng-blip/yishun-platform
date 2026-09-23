-- 先备份，再执行；兼容重复执行，不修改原闲鱼链接。
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_auction' AND column_name='mini_app_id'), 'SELECT 1', 'ALTER TABLE app_auction ADD COLUMN mini_app_id VARCHAR(32) NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_auction' AND column_name='mini_path'), 'SELECT 1', 'ALTER TABLE app_auction ADD COLUMN mini_path TEXT NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_auction' AND column_name='mini_short_link'), 'SELECT 1', 'ALTER TABLE app_auction ADD COLUMN mini_short_link TEXT NULL');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 回退旧程序即可，保留新增列和业务数据。
