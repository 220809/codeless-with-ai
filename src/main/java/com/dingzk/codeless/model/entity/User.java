package com.dingzk.codeless.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import java.io.Serial;

import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户表(user)实体类
 *
 * @author ding
 * @date 2025/12/4 15:23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
    @Column("id")
    private Long id;

    /**
     * 用户昵称
     */
    @Column("username")
    private String username;

    /**
     * 用户简介
     */
    @Column("user_intro")
    private String userIntro;

    /**
     * 账号
     */
    @Column("user_account")
    private String userAccount;

    /**
     * 密码
     */
    @Column("password")
    private String password;

    /**
     * 用户头像
     */
    @Column("avatar_url")
    private String avatarUrl;

    /**
     * 性别 0-未知，1-男，2-女
     */
    @Column("gender")
    private Integer gender;

    /**
     * 用户角色：0-user / 1-admin
     */
    @Column("user_role")
    private Integer userRole;

    /**
     * 编辑时间
     */
    @Column("edit_time")
    private LocalDateTime editTime;

    /**
     * 创建时间
     */
    @Column("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Column("update_time")
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-未删除，1-已删除
     */
    @Column(value = "deleted", isLogicDelete = true)
    private Integer deleted;

}
