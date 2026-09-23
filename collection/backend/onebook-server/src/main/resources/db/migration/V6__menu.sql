-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
    '批量交易卖单管理', '', 2, 0, 5042,
    'sell-order', '', 'app/sellorder/index', 0, 'SellOrder'
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
    '批量交易卖单查询', 'app:sell-order:query', 3, 1, @parentId,
    '', '', '', 0
);

-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '批量交易买单管理', '', 2, 0, 5042,
           'buy-order', '', 'app/buyorder/index', 0, 'BuyOrder'
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
           '批量交易买单查询', 'app:buy-order:query', 3, 1, @parentId,
           '', '', '', 0
       );
