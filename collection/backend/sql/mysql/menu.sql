-- 藏品菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '藏品登记管理', '', 2, 0, 5042,
           'collection', '', 'app/collection/index', 0, 'Collection'
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
           '藏品登记查询', 'app:collection:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '藏品审核', 'app:collection:update', 3, 3, @parentId,
           '', '', '', 0
       );

-- 藏品分类菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '藏品分类管理', '', 2, 0, 5042,
           'collection-category', '', 'app/category/index', 0, 'CollectionCategory'
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
           '藏品分类查询', 'app:collection-category:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '藏品分类创建', 'app:collection-category:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '藏品分类更新', 'app:collection-category:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '藏品分类删除', 'app:collection-category:delete', 3, 4, @parentId,
           '', '', '', 0
       );

-- 一口价菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '一口价管理', '', 2, 0, 5042,
           'yikoujia', '', 'app/yikoujia/index', 0, 'Yikoujia'
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
           '一口价查询', 'app:yikoujia:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '一口价更新', 'app:yikoujia:update', 3, 3, @parentId,
           '', '', '', 0
       );

-- 快速变现菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '快速变现管理', '', 2, 0, 5042,
           'fast-trade', '', 'app/fasttrade/index', 0, 'FastTrade'
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
           '快速变现查询', 'app:fast-trade:query', 3, 1, @parentId,
           '', '', '', 0
       );

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '快速变现更新', 'app:fast-trade:update', 3, 3, @parentId,
           '', '', '', 0
       );

-- 取回菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '取回管理', '', 2, 0, 5042,
           'getback', '', 'app/getback/index', 0, 'Getback'
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
           '取回查询', 'app:getback:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '取回更新', 'app:getback:update', 3, 3, @parentId,
           '', '', '', 0
       );

-- 广告菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '广告管理', '', 2, 0, 5042,
           'ads', '', 'app/ads/index', 0, 'Ads'
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
           '广告查询', 'app:ads:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '广告创建', 'app:ads:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '广告更新', 'app:ads:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '广告删除', 'app:ads:delete', 3, 4, @parentId,
           '', '', '', 0
       );

-- 兑换品菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '兑换品管理', '', 2, 0, 5042,
           'exchange', '', 'app/exchange/index', 0, 'Exchange'
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
           '兑换品查询', 'app:exchange:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '兑换品创建', 'app:exchange:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '兑换品更新', 'app:exchange:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '兑换品删除', 'app:exchange:delete', 3, 4, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '兑换品导出', 'app:exchange:export', 3, 5, @parentId,
           '', '', '', 0
       );


-- 兑换记录菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '兑换记录管理', '', 2, 0, 5042,
           'exchange-log', '', 'app/exchangelog/index', 0, 'ExchangeLog'
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
           '兑换记录查询', 'app:exchange-log:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '兑换记录创建', 'app:exchange-log:create', 3, 2, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '兑换记录更新', 'app:exchange-log:update', 3, 3, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '兑换记录删除', 'app:exchange-log:delete', 3, 4, @parentId,
           '', '', '', 0
       );

-- 能量石兑换记录菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '能量石兑换管理', '', 2, 0, 5042,
           'stone-exchange', '', 'app/stoneexchange/index', 0, 'StoneExchange'
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
           '能量石兑换查询', 'app:stone-exchange:query', 3, 1, @parentId,
           '', '', '', 0
       );
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '能量石兑换更新', 'app:stone-exchange:update', 3, 3, @parentId,
           '', '', '', 0
       );

-- 能量石记录菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '能量石记录', '', 2, 0, 5042,
           'stone-record', '', 'app/stonerecord/index', 0, 'StoneRecord'
       );

SELECT @parentId := LAST_INSERT_ID();

INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '能量石记录查询', 'app:stone-record:query', 3, 1, @parentId,
           '', '', '', 0
       );
