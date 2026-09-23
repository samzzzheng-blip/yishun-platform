-- 先备份、检查列是否存在，再执行一次。回退旧代码时保留新增可空列。
ALTER TABLE app_stone_record
 ADD COLUMN exchange_log_id bigint NULL COMMENT '明确关联的兑换订单，不猜测历史匹配',
 ADD COLUMN exchange_name varchar(512) NULL COMMENT '兑换时商品名称快照',
 ADD COLUMN exchange_quantity int NULL COMMENT '兑换商品数量';
