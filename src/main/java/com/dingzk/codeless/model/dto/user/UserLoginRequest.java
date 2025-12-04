package com.dingzk.codeless.model.dto.user;

import lombok.Data;

import java.io.Serial;

/**
 * 用户登录请求参数包装类
 *
 * @author ding
 * @date 2025/12/4 15:32
 */
@Data
public class UserLoginRequest implements UserBaseDto {
    private String userAccount;
    private String password;

    @Serial
    private static final long serialVersionUID = 1L;
}
