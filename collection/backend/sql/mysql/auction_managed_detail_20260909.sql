-- 上线前执行；旧版本回退无需删除新增列，保留业务数据。
ALTER TABLE app_auction
 ADD COLUMN goofish_managed_product_id varchar(64) NULL COMMENT '闲管家product_id，与闲鱼item_id分离',
 ADD COLUMN goofish_detail text NULL COMMENT '商品详情快照，不作为竞拍成交依据';
