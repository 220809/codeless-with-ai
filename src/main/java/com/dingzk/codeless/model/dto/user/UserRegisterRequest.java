package com.dingzk.codeless.model.dto.user;

import lombok.Data;

import java.io.Serial;

/**
 * 用户注册请求参数包装类
 *
 * @author ding
 * @date 2025/12/4 15:32
 */
@Data
public class UserRegisterRequest implements UserBaseDto {
    @Serial
    private static final long serialVersionUID = 3454284554170795379L;
    /**
     * 账号
     */
    private String userAccount;
    /**
     * 密码
     */
    private String password;
    /**
     * 确认密码
     */
    private String checkedPassword;
}
