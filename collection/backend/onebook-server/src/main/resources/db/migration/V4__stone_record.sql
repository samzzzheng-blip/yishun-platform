create table app_stone_record
(
    id          bigint auto_increment comment '编号'
        primary key,
    user_id     bigint                                not null comment '用户id',
    amount      int                                   null comment '数量',
    type        tinyint default 0 comment '类型',
    creator     varchar(64) default ''                null comment '创建者',
    create_time datetime    default CURRENT_TIMESTAMP not null comment '创建时间',
    updater     varchar(64) default ''                null comment '更新者',
    update_time datetime    default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     bit         default b'0'              not null comment '是否删除'
)
    comment '能量石记录' collate = utf8mb4_unicode_ci;
-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.

-- Data row omitted from source archive; restore data from protected backup.


    