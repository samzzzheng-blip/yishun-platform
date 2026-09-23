-- 仅增加原始分享文案，不覆盖已有商品链接/库存/状态。重复执行安全。
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='app_auction' AND column_name='share_text'), 'SELECT 1', 'ALTER TABLE app_auction ADD COLUMN share_text TEXT NULL COMMENT ''闲鱼App完整分享文案''');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
-- 回退旧程序即可，保留字段和数据；无需执行 mini_jump 脚本。
