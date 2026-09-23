-- 一口价商品：小程序 + 闲鱼双端在售与唯一成交渠道留痕
-- 执行前请先完整备份数据库。本迁移只新增字段和索引，不删除历史数据。
SET NAMES utf8mb4;

ALTER TABLE `app_yikoujia`
  ADD COLUMN `sale_channel` varchar(16) DEFAULT NULL
    COMMENT '实际成交渠道 MINIAPP/GOOFISH/CONFLICT' AFTER `product_id`,
  ADD COLUMN `sold_at` datetime DEFAULT NULL
    COMMENT '成交时间' AFTER `sale_channel`,
  ADD COLUMN `goofish_status` int DEFAULT NULL
    COMMENT '闲鱼最后商品状态' AFTER `sold_at`,
  ADD COLUMN `last_sync_time` datetime DEFAULT NULL
    COMMENT '最后闲鱼同步时间' AFTER `goofish_status`,
  ADD COLUMN `sync_remark` varchar(255) DEFAULT NULL
    COMMENT '同步或冲突说明' AFTER `last_sync_time`,
  ADD KEY `idx_yikoujia_channel_status` (`status`, `sale_channel`, `update_time`);

-- 为历史成交记录补充渠道。状态 1 是小程序内已成交，状态 4 是闲鱼检测到已售。
UPDATE `app_yikoujia`
SET `sale_channel` = 'MINIAPP', `sold_at` = COALESCE(`sold_at`, `update_time`)
WHERE `status` = 1 AND (`sale_channel` IS NULL OR `sale_channel` = '');

UPDATE `app_yikoujia`
SET `sale_channel` = 'GOOFISH', `sold_at` = COALESCE(`sold_at`, `update_time`)
WHERE `status` = 4 AND (`sale_channel` IS NULL OR `sale_channel` = '');
