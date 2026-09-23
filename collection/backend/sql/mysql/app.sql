DROP TABLE IF EXISTS `app_category`;
CREATE TABLE `app_category`  (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类编号',
 `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
 `user_id` bigint NOT NULL DEFAULT 0 COMMENT '自定义分类的用户id，系统自带的为0',
 `copy_id` bigint NULL DEFAULT 0 COMMENT '复制的哪个id',
 `pic_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片地址',
 `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '藏品分类表';
-- Data row omitted from source archive; restore data from protected backup.


DROP TABLE IF EXISTS `app_collection`;
CREATE TABLE `app_collection`  (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '藏品编号',
 `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品名',
 `user_id` bigint NOT NULL COMMENT '用户id',
 `category_id` bigint NOT NULL COMMENT '分类id',
 `category_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '分类名',
 `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待审核 1通过 2驳回）',
 `pic_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片地址',
 `stock` int NULL DEFAULT NULL COMMENT '数量',
 `real_stock` int NULL DEFAULT NULL COMMENT '实际数量',
 `trade_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0初始状态 1快速变现 2一口价）',
 `getback_status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0初始状态 1取回 2发货）',
 `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '藏品登记表';



DROP TABLE IF EXISTS `app_sell_order`;
CREATE TABLE `app_sell_order`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`user_id` bigint NOT NULL COMMENT '用户id',
`category_id` bigint NOT NULL COMMENT '藏品分类id',
`amount` int NULL DEFAULT NULL COMMENT '剩余未成交数量',
`deal_amount` int NULL DEFAULT NULL COMMENT '交易数量',
`price` int NOT NULL DEFAULT -1 COMMENT '价格，单位：分',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批量交易卖单表';

DROP TABLE IF EXISTS `app_buy_order`;
CREATE TABLE `app_buy_order`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`user_id` bigint NOT NULL COMMENT '用户id',
`category_id` bigint NOT NULL COMMENT '藏品分类id',
`amount` int NULL DEFAULT NULL COMMENT '剩余未成交数量',
`deal_amount` int NULL DEFAULT NULL COMMENT '交易数量',
`price` int NOT NULL DEFAULT -1 COMMENT '价格，单位：分',
`deal_price` int NOT NULL DEFAULT 0 COMMENT '实际成交价格，单位：分',
`status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态（0未支付，1已支付）',
`pay_order_id` bigint NULL DEFAULT NULL COMMENT '支付订单编号',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批量交易买单表';

DROP TABLE IF EXISTS `app_deal_order`;
CREATE TABLE `app_deal_order`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`category_id` bigint NOT NULL COMMENT '藏品分类id',
`amount` int NULL DEFAULT NULL COMMENT '数量',
`price` int NOT NULL DEFAULT -1 COMMENT '价格，单位：分',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批量交易成交表';


DROP TABLE IF EXISTS `app_fast_trade`;
CREATE TABLE `app_fast_trade`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`user_id` bigint NOT NULL COMMENT '用户id',
`status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0已下架 1小程序成交 2已撤回 3双端在售 4闲鱼成交 6双端成交冲突）',
`price` int NOT NULL DEFAULT -1 COMMENT '价格，单位：分',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '快速变现';

DROP TABLE IF EXISTS `app_fast_trade_item`;
CREATE TABLE `app_fast_trade_item`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`fast_id` bigint NOT NULL COMMENT '交易id',
`collection_id` bigint NOT NULL COMMENT '藏品id',
`pic_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片地址',
`amount` int NULL DEFAULT NULL COMMENT '数量',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '快速变现藏品表';

DROP TABLE IF EXISTS `app_yikoujia`;
CREATE TABLE `app_yikoujia`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`user_id` bigint NOT NULL COMMENT '用户id',
`price` int NOT NULL DEFAULT -1 COMMENT '价格，单位：分',
`status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待交易 1已完成）',
`collection_id` bigint NOT NULL COMMENT '藏品id',
`pic_url` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '图片地址',
`amount` int NULL DEFAULT NULL COMMENT '数量',
`name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '品名',
`product_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '产品id',
`sale_channel` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '实际成交渠道 MINIAPP/GOOFISH/CONFLICT',
`sold_at` datetime NULL COMMENT '成交时间',
`goofish_status` int NULL COMMENT '闲鱼最后商品状态',
`last_sync_time` datetime NULL COMMENT '最后闲鱼同步时间',
`sync_remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '同步或冲突说明',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '一口价';

DROP TABLE IF EXISTS `app_yikoujia_item`;
CREATE TABLE `app_yikoujia_item`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`ykj_id` bigint NOT NULL COMMENT '交易id',
`collection_id` bigint NOT NULL COMMENT '藏品id',
`pic_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片地址',
`amount` int NULL DEFAULT NULL COMMENT '数量',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '一口价藏品表';

DROP TABLE IF EXISTS `app_getback`;
CREATE TABLE `app_getback`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`user_id` bigint NOT NULL COMMENT '用户id',
`status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待发货 1已发货）',
`deliver_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '快递单号',
`receiver_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '收件人名称',
`receiver_mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '收件人手机',
`receiver_area_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '收件人地区',
`receiver_detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '收件人详细地址',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '取回';

DROP TABLE IF EXISTS `app_getback_item`;
CREATE TABLE `app_getback_item`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`getback_id` bigint NOT NULL COMMENT '取回id',
`collection_id` bigint NOT NULL COMMENT '藏品id',
`pic_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片地址',
`amount` int NULL DEFAULT NULL COMMENT '数量',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '取回藏品表';

DROP TABLE IF EXISTS `app_collection_transfer`;
CREATE TABLE `app_collection_transfer`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`user_id` bigint NOT NULL COMMENT '用户id',
`to_user_id` bigint NOT NULL COMMENT '被转移用户id',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '转移记录';

DROP TABLE IF EXISTS `app_collection_transfer_item`;
CREATE TABLE `app_collection_transfer_item`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`transfer_id` bigint NOT NULL COMMENT '转移id',
`collection_id` bigint NOT NULL COMMENT '藏品id',
`amount` int NULL DEFAULT NULL COMMENT '数量',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '转移藏品表';

DROP TABLE IF EXISTS `app_ads`;
CREATE TABLE `app_ads`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`pic_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片地址',
`status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0开启 1关闭）',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '广告表';

DROP TABLE IF EXISTS `app_exchange`;
CREATE TABLE `app_exchange`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '兑换品名称',
`pic_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片',
`status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0开启 1关闭）',
`stock` int NULL DEFAULT NULL COMMENT '库存',
`amount` int NULL DEFAULT NULL COMMENT '兑换数量',
`ordinal_position` int NOT NULL COMMENT '排序',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '兑换品';

CREATE TABLE `app_exchange_question`  (
 `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
 `exchange_id` bigint NOT NULL COMMENT '兑换品id',
 `question` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题',
 `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
 `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '兑换品问题';

DROP TABLE IF EXISTS `app_exchange_log`;
CREATE TABLE `app_exchange_log`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`exchange_id` bigint NOT NULL COMMENT '兑换品id',
`exchange_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '兑换品名称',
`user_id` bigint NOT NULL COMMENT '用户id',
`amount` int NULL DEFAULT NULL COMMENT '兑换数量',
`mobile` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
`status` tinyint NOT NULL DEFAULT 0 COMMENT '状态（0待兑换 1已发货，2已收货）',
`answer` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '答案',
`deliver_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL COMMENT '快递单号',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '兑换记录';

CREATE TABLE `app_energy_stone`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`user_id` bigint NOT NULL COMMENT '用户id',
`amount` int NULL DEFAULT NULL COMMENT '数量',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '能量石';

CREATE TABLE `app_stone_exchange`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`user_id` bigint NOT NULL COMMENT '用户id',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '能量石兑换表';

CREATE TABLE `app_stone_exchange_item`  (
`id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
`stone_exchange_id` bigint NOT NULL COMMENT '订单id',
`collection_id` bigint NOT NULL COMMENT '藏品id',
`pic_url` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '图片地址',
`creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
`updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
`deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '能量石兑换藏品表';



DROP TABLE IF EXISTS `app_ykj_order`;
CREATE TABLE `app_ykj_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `ykj_id` bigint NOT NULL COMMENT '一口价商品id',
  `price` int NOT NULL DEFAULT -1 COMMENT '价格，单位：分',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态（0未支付，1已支付）',
  `pay_order_id` bigint NULL DEFAULT NULL COMMENT '支付订单编号',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '一口价买单表';


