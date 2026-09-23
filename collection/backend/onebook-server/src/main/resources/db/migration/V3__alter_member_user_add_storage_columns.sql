ALTER TABLE `member_user` ADD COLUMN `storage_id` bigint NULL DEFAULT 0 COMMENT '寄存套餐ID' AFTER `group_id`;
ALTER TABLE `member_user` ADD COLUMN `expire_time` datetime NULL DEFAULT null COMMENT '套餐失效时间' AFTER `storage_id`;
ALTER TABLE `member_user` ADD COLUMN `over_days` int NOT NULL DEFAULT 0 COMMENT '逾期天数' AFTER `expire_time`;
ALTER TABLE `member_user` ADD COLUMN `debt_amount` int NOT NULL DEFAULT 0 COMMENT '逾期金额' AFTER `over_days`;
