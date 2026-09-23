-- 竞价市场：执行前请先备份数据库。金额单位均为分。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `app_auction` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '竞价ID',
  `seller_id` bigint NOT NULL COMMENT '卖家ID',
  `collection_id` bigint NOT NULL COMMENT '藏品ID',
  `collection_name` varchar(255) NOT NULL COMMENT '藏品名称快照',
  `category_name` varchar(255) DEFAULT NULL COMMENT '分类名称快照',
  `pic_url` varchar(2000) DEFAULT NULL COMMENT '封面快照',
  `pic_urls` json DEFAULT NULL COMMENT '拍品图片快照',
  `amount` int NOT NULL DEFAULT 1 COMMENT '数量',
  `start_price` int NOT NULL COMMENT '起拍价',
  `min_increment` int NOT NULL COMMENT '最低加价',
  `current_price` int NOT NULL COMMENT '当前价',
  `highest_bid_id` bigint DEFAULT NULL COMMENT '最高出价ID',
  `highest_bidder_id` bigint DEFAULT NULL COMMENT '最高出价人ID',
  `bid_count` int NOT NULL DEFAULT 0 COMMENT '出价次数',
  `goofish_product_id` varchar(64) DEFAULT NULL COMMENT '闲管家商品ID',
  `goofish_url` varchar(1000) DEFAULT NULL COMMENT '闲鱼竞拍跳转地址',
  `sync_error` varchar(500) DEFAULT NULL COMMENT '最近发布或同步错误',
  `last_sync_time` datetime(3) DEFAULT NULL COMMENT '最近同步时间',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '0待人工发布 1闲鱼竞拍中 3成交 4流拍 5取消 6同步异常 7审核驳回',
  `review_time` datetime(3) DEFAULT NULL COMMENT '人工审核时间',
  `review_user_id` bigint DEFAULT NULL COMMENT '审核管理员ID',
  `review_reject_reason` varchar(200) DEFAULT NULL COMMENT '送拍驳回原因',
  `delist_status` tinyint NOT NULL DEFAULT 0 COMMENT '下架审核：0无 1待审核 2已同意 3已拒绝',
  `delist_apply_time` datetime(3) DEFAULT NULL COMMENT '申请下架时间',
  `delist_audit_time` datetime(3) DEFAULT NULL COMMENT '下架审核时间',
  `delist_audit_user_id` bigint DEFAULT NULL COMMENT '审核管理员ID',
  `delist_reject_reason` varchar(200) DEFAULT NULL COMMENT '下架拒绝原因',
  `end_time` datetime(3) NOT NULL COMMENT '截止时间',
  `settled_time` datetime(3) DEFAULT NULL COMMENT '结算时间',
  `creator` varchar(64) DEFAULT '', `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updater` varchar(64) DEFAULT '', `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`), KEY `idx_status_end_time` (`status`,`end_time`),
  KEY `idx_seller_status` (`seller_id`,`status`), KEY `idx_collection_id` (`collection_id`),
  UNIQUE KEY `uk_goofish_product_id` (`goofish_product_id`),
  KEY `idx_delist_status` (`delist_status`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='藏品竞价';

CREATE TABLE IF NOT EXISTS `app_auction_bid` (
  `id` bigint NOT NULL AUTO_INCREMENT, `auction_id` bigint NOT NULL, `bidder_id` bigint NOT NULL,
  `amount` int NOT NULL, `status` tinyint NOT NULL COMMENT '1领先 2被超越 3成交',
  `request_id` varchar(64) NOT NULL COMMENT '幂等请求ID',
  `creator` varchar(64) DEFAULT '', `create_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updater` varchar(64) DEFAULT '', `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `deleted` bit(1) NOT NULL DEFAULT b'0', PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bid_request` (`bidder_id`,`request_id`), KEY `idx_auction_id` (`auction_id`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞价出价记录';

CREATE TABLE IF NOT EXISTS `app_auction_settlement` (
  `id` bigint NOT NULL AUTO_INCREMENT, `auction_id` bigint NOT NULL, `seller_id` bigint NOT NULL,
  `buyer_id` bigint NOT NULL, `bid_id` bigint NOT NULL, `gross_amount` int NOT NULL,
  `fee_amount` int NOT NULL DEFAULT 0, `seller_income` int NOT NULL, `status` tinyint NOT NULL,
  `fail_reason` varchar(1000) DEFAULT NULL, `retry_count` int NOT NULL DEFAULT 0,
  `settled_time` datetime(3) DEFAULT NULL, `creator` varchar(64) DEFAULT '',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP, `updater` varchar(64) DEFAULT '',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` bit(1) NOT NULL DEFAULT b'0', PRIMARY KEY (`id`), UNIQUE KEY `uk_auction_id` (`auction_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞价结算单';
-- Data row omitted from source archive; restore data from protected backup.


SET @auctionRootMenuId = COALESCE(
  (SELECT `parent_id` FROM `system_menu` WHERE `component` IN ('app/yikoujia/index','app/collection/index') AND `deleted`=b'0' ORDER BY `id` LIMIT 1),
  0
);
INSERT INTO `system_menu` (`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`status`,`component_name`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '竞价管理','',2,20,@auctionRootMenuId,'auction','','app/auction/index',0,'Auction','1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `component`='app/auction/index' AND `deleted`=b'0');
SET @auctionMenuId = (SELECT id FROM `system_menu` WHERE `component`='app/auction/index' AND `deleted`=b'0' ORDER BY id DESC LIMIT 1);
INSERT INTO `system_menu` (`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`status`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '竞价查询','app:auction:query',3,1,@auctionMenuId,'','','',0,'1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='app:auction:query' AND `deleted`=b'0');
INSERT INTO `system_menu` (`name`,`permission`,`type`,`sort`,`parent_id`,`path`,`icon`,`component`,`status`,`creator`,`create_time`,`updater`,`update_time`,`deleted`)
SELECT '竞价处理','app:auction:update',3,2,@auctionMenuId,'','','',0,'1',NOW(),'1',NOW(),b'0'
WHERE NOT EXISTS (SELECT 1 FROM `system_menu` WHERE `permission`='app:auction:update' AND `deleted`=b'0');

SET @auctionQueryMenuId = (SELECT id FROM `system_menu` WHERE `permission`='app:auction:query' AND `deleted`=b'0' ORDER BY id DESC LIMIT 1);
SET @auctionUpdateMenuId = (SELECT id FROM `system_menu` WHERE `permission`='app:auction:update' AND `deleted`=b'0' ORDER BY id DESC LIMIT 1);
-- Data row omitted from source archive; restore data from protected backup.

