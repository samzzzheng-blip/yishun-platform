-- 藏品分类变更记录表
CREATE TABLE IF NOT EXISTS `app_collection_category_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '编号',
  `category_id` bigint NOT NULL COMMENT '分类编号',
  `category_name` varchar(64) NOT NULL COMMENT '分类名称',
  `collection_id` bigint NOT NULL COMMENT '藏品编号',
  `collection_name` varchar(128) NOT NULL COMMENT '藏品名称',
  `user_id` bigint NOT NULL COMMENT '用户编号',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '操作人名称',
  `creator` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_collection_id` (`collection_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='藏品分类变更记录';


INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '藏品分类变更记录', '', 2, 0, 5042,
           'category-changelog', '', 'app/category/ChangeLog', 0, 'CategoryChangeLog'
       );

-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '藏品分类变更记录查询', 'app:category-changelog:query', 3, 1, @parentId,
           '', '', '', 0
       );


INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '能量石变更记录', '', 2, 0, 5042,
           'stone-record', '', 'app/stonerecord/index', 0, 'StoneRecord'
       );

-- 按钮父菜单ID
-- 暂时只支持 MySQL。如果你是 Oracle、PostgreSQL、SQLServer 的话，需要手动修改 @parentId 的部分的代码
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '能量石变更记录查询', 'app:stone-record:query', 3, 1, @parentId,
           '', '', '', 0
       );