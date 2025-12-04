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