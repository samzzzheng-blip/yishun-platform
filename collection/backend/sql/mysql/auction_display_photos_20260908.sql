-- 部署前执行；保留原 pic_url/pic_urls 和藏品图片。允许重复执行。
SET @ddl = IF(EXISTS(SELECT 1 FROM information_schema.columns
  WHERE table_schema = DATABASE() AND table_name = 'app_auction' AND column_name = 'display_pic_urls'),
  'SELECT 1', 'ALTER TABLE app_auction ADD COLUMN display_pic_urls TEXT NULL COMMENT ''人工维护的拍卖展示照片，首图为封面''');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
-- 回退仅恢复旧程序；保留新增列，不删除照片或业务数据。
