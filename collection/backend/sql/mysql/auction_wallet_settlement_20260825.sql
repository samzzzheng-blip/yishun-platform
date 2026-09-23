-- 闲鱼竞拍成交确认与钱包结算安全升级
-- 执行前请先完整备份数据库。所有金额均以“分”保存。

ALTER TABLE `app_auction`
  MODIFY COLUMN `status` tinyint NOT NULL DEFAULT 0
  COMMENT '0待人工发布 1闲鱼竞拍中 2待确认成交 3已售出且入钱包 4流拍 5取消 6同步异常 7审核驳回';

ALTER TABLE `app_auction_settlement`
  MODIFY COLUMN `buyer_id` bigint DEFAULT NULL,
  MODIFY COLUMN `bid_id` bigint DEFAULT NULL,
  ADD COLUMN `fee_rate` int NOT NULL DEFAULT 0 COMMENT '成交确认时锁定的手续费百分比' AFTER `gross_amount`,
  ADD COLUMN `confirm_user_id` bigint DEFAULT NULL COMMENT '确认成交管理员ID' AFTER `status`,
  ADD COLUMN `confirm_remark` varchar(200) DEFAULT NULL COMMENT '人工核对说明' AFTER `confirm_user_id`;

ALTER TABLE `trade_brokerage_withdraw`
  ADD COLUMN `auction_id` bigint DEFAULT NULL COMMENT '竞拍提现来源ID' AFTER `user_id`,
  ADD COLUMN `source_collection_name` varchar(255) DEFAULT NULL COMMENT '售卖商品快照' AFTER `auction_id`,
  ADD COLUMN `source_gross_amount` int DEFAULT NULL COMMENT '成交金额快照（分）' AFTER `source_collection_name`,
  ADD COLUMN `source_fee_rate` int DEFAULT NULL COMMENT '竞拍手续费百分比快照' AFTER `source_gross_amount`,
  ADD COLUMN `source_fee_amount` int DEFAULT NULL COMMENT '竞拍手续费快照（分）' AFTER `source_fee_rate`,
  ADD COLUMN `audit_user_id` bigint DEFAULT NULL COMMENT '二次核对管理员ID' AFTER `audit_time`,
  ADD KEY `idx_auction_withdraw` (`auction_id`,`status`);

-- 手续费由后台“基础设施/参数配置”维护，键名固定为 auctionfee，值为 0-99 的整数百分比。
-- 不自动写入默认值：未明确配置时，确认成交接口会拒绝入账，避免误按 0 手续费结算。
