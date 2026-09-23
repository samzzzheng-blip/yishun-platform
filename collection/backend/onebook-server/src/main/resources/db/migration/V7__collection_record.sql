-- 藏品变更记录表
create table app_collection_record
(
    id              bigint auto_increment comment '编号'
        primary key,
    user_id         bigint                                not null comment '用户id',
    category_id     bigint                                null comment '藏品分类id',
    amount          int                                   null comment '数量',
    type            tinyint      default 0 comment '类型：1-用户新增 2-管理员新增 3-删除 4-兑换能量石 5-买 6-卖 7-取回',
    creator         varchar(64)  default ''                null comment '创建者',
    create_time     datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updater         varchar(64)  default ''                null comment '更新者',
    update_time     datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted         bit          default b'0'              not null comment '是否删除'
)
    comment '藏品变更记录' collate = utf8mb4_unicode_ci;

-- 菜单 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status, component_name
)
VALUES (
           '藏品变更记录', '', 2, 0, 5042,
           'collection-record', '', 'app/collectionrecord/index', 0, 'CollectionRecord'
       );

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
INSERT INTO system_menu(
    name, permission, type, sort, parent_id,
    path, icon, component, status
)
VALUES (
           '藏品变更记录查询', 'app:collection-record:query', 3, 1, @parentId,
           '', '', '', 0
       );
