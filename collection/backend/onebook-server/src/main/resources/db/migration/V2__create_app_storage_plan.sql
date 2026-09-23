CREATE TABLE IF NOT EXISTS `app_storage_plan` (
    `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `min_count` int NOT NULL DEFAULT 0 COMMENT '藏品数量下限',
    `max_count` int NOT NULL DEFAULT 0 COMMENT '藏品数量上限',
    `monthly_price` int NOT NULL DEFAULT 0 COMMENT '月费（能量石）',
    `yearly_price` int NOT NULL DEFAULT 0 COMMENT '年费（能量石）',
    `creator` varchar(64) DEFAULT '' COMMENT '创建者',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updater` varchar(64) DEFAULT '' COMMENT '更新者',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '寄存容量套餐表';
-- Data row omitted from source archive; restore data from protected backup.
