-- 闲鱼竞拍改为“小程序申请 -> 后台人工发布 -> 录入链接 -> 系统同步”。
-- 执行前请先备份数据库。
SET NAMES utf8mb4;

ALTER TABLE `app_auction`
  ADD COLUMN `review_time` datetime(3) DEFAULT NULL COMMENT '人工审核时间' AFTER `status`,
  ADD COLUMN `review_user_id` bigint DEFAULT NULL COMMENT '审核管理员ID' AFTER `review_time`,
  ADD COLUMN `review_reject_reason` varchar(200) DEFAULT NULL COMMENT '送拍驳回原因' AFTER `review_user_id`;

ALTER TABLE `app_auction`
  MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 0
    COMMENT '0待人工发布 1闲鱼竞拍中 3成交 4流拍 5取消 6同步异常 7审核驳回';

-- 数据库层防止并发操作把同一个闲鱼竞拍绑定给两条申请；NULL 可重复。
ALTER TABLE `app_auction`
  ADD UNIQUE KEY `uk_goofish_product_id` (`goofish_product_id`);

-- 旧版自动发布失败且没有远端商品的记录，回到待人工发布。
UPDATE `app_auction`
SET `status` = 0, `sync_error` = NULL, `last_sync_time` = NULL
WHERE `status` = 6 AND (`goofish_product_id` IS NULL OR `goofish_product_id` = '') AND `deleted` = b'0';

-- 已有商品编号的旧异常记录保留为异常，由管理员重新录入正确竞拍链接。
UPDATE `infra_job`
SET `name` = '闲鱼竞拍数据同步', `update_time` = NOW()
WHERE `handler_name` = 'auctionCloseJob' AND `deleted` = b'0';
