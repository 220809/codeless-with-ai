# 创建数据库
create database if not exists codeless;
# 使用数据库
use codeless;
# 创建用户表
create table if not exists user
(
    id           bigint auto_increment comment '用户id' primary key,
    username     varchar(256)  default '新用户'                     not null comment '用户昵称',
    user_intro   varchar(1024) default '这个用户很懒，什么也没留下。' not null comment '用户简介',
    user_account varchar(256)                                       not null comment '账号',
    password     varchar(256)                                       not null comment '密码',
    avatar_url   varchar(1024) default 'https://cdn.pixabay.com/photo/2016/08/08/09/17/avatar-1577909_1280.png'
                                                                    not null comment '用户头像',
    gender       tinyint       default 0                            not null comment '性别 0-未知，1-男，2-女',
    user_role    tinyint       default 0                            not null comment '用户角色：0-user / 1-admin',
    edit_time    datetime      default CURRENT_TIMESTAMP            not null comment '编辑时间',
    create_time  datetime      default CURRENT_TIMESTAMP            not null comment '创建时间',
    update_time  datetime      default CURRENT_TIMESTAMP            not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted      tinyint       default 0                            not null comment '是否删除 0-未删除，1-已删除',
    constraint uni_user_account
        unique (user_account),
    index idx_username (username)
) comment '用户表' collate utf8mb4_general_ci;

-- 应用表
create table app
(
    id           bigint auto_increment comment 'id' primary key,
    name      varchar(256)                       null comment '应用名称',
    cover        varchar(512)                       null comment '应用封面',
    initial_prompt   text                               null comment '应用初始化的 prompt',
    gen_file_Type  varchar(64)                        null comment '代码生成类型（枚举）',
    deploy_key    varchar(64)                        null comment '部署标识',
    deployed_time datetime                           null comment '部署时间',
    priority     int      default 0                 not null comment '优先级',
    user_id       bigint                             not null comment '创建用户id',
    edit_time     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    create_time   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    deleted     tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_deploy_key (deploy_key), -- 确保部署标识唯一
    INDEX idx_name (name),         -- 提升基于应用名称的查询性能
    INDEX idx_user_id (user_id)            -- 提升基于用户 ID 的查询性能
) comment '应用' collate = utf8mb4_general_ci;