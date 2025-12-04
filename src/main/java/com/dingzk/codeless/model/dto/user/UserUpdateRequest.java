package com.dingzk.codeless.model.dto.user;

import lombok.Data;

import java.io.Serial;

/**
 * 用户更新请求参数包装类
 *
 * @author ding
 * @date 2025/12/4 15:33
 */
@Data
public class UserUpdateRequest implements UserBaseDto {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户简介
     */
    private String userIntro;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 性别 0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 用户角色：0-user / 1-admin
     */
    private Integer userRole;

    @Serial
    private static final long serialVersionUID = 1L;
}
