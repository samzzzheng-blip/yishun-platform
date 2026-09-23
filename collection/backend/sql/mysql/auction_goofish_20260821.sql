-- 将原站内竞价改为闲鱼竞拍展示。执行前请先备份数据库。
SET NAMES utf8mb4;

ALTER TABLE `app_auction`
  ADD COLUMN `pic_urls` json DEFAULT NULL COMMENT '拍品图片快照' AFTER `pic_url`,
  ADD COLUMN `goofish_product_id` varchar(64) DEFAULT NULL COMMENT '闲管家商品ID' AFTER `bid_count`,
  ADD COLUMN `goofish_url` varchar(1000) DEFAULT NULL COMMENT '闲鱼竞拍跳转地址' AFTER `goofish_product_id`,
  ADD COLUMN `sync_error` varchar(500) DEFAULT NULL COMMENT '最近发布或同步错误' AFTER `goofish_url`,
  ADD COLUMN `last_sync_time` datetime(3) DEFAULT NULL COMMENT '最近同步时间' AFTER `sync_error`;

ALTER TABLE `app_auction`
  MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 0
    COMMENT '0待发布 1闲鱼竞拍中 3成交 4流拍 5取消 6发布失败';

UPDATE `infra_job`
SET `name` = '闲鱼竞拍发布与同步', `cron_expression` = '0/10 * * * * ?', `update_time` = NOW()
WHERE `handler_name` = 'auctionCloseJob' AND `deleted` = b'0';

-- 旧版尚无出价的进行中记录改为待发布，由任务发布到闲鱼。
UPDATE `app_auction`
SET `status` = 0, `current_price` = `start_price`, `bid_count` = 0,
    `highest_bid_id` = NULL, `highest_bidder_id` = NULL
WHERE `status` = 1 AND `bid_count` = 0 AND `goofish_product_id` IS NULL AND `deleted` = b'0';

UPDATE `app_auction`
SET `pic_urls` = JSON_ARRAY(`pic_url`)
WHERE `pic_urls` IS NULL AND `pic_url` IS NOT NULL AND `pic_url` <> '' AND `deleted` = b'0';

-- 已存在站内出价或冻结资金的记录不要自动迁移，须先按旧流程人工处理完毕。
