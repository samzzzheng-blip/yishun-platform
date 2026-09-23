-- 闲鱼竞拍下架申请与后台审核。执行前请先备份数据库。
SET NAMES utf8mb4;

ALTER TABLE `app_auction`
  ADD COLUMN `delist_status` tinyint NOT NULL DEFAULT 0
    COMMENT '下架审核：0无 1待审核 2已同意 3已拒绝' AFTER `status`,
  ADD COLUMN `delist_apply_time` datetime(3) DEFAULT NULL
    COMMENT '申请下架时间' AFTER `delist_status`,
  ADD COLUMN `delist_audit_time` datetime(3) DEFAULT NULL
    COMMENT '下架审核时间' AFTER `delist_apply_time`,
  ADD COLUMN `delist_audit_user_id` bigint DEFAULT NULL
    COMMENT '审核管理员ID' AFTER `delist_audit_time`,
  ADD COLUMN `delist_reject_reason` varchar(200) DEFAULT NULL
    COMMENT '下架拒绝原因' AFTER `delist_audit_user_id`,
  ADD KEY `idx_delist_status` (`delist_status`, `create_time`);
