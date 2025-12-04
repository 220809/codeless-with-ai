package com.dingzk.codeless.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户角色枚举
 *
 * @author ding
 * @date 2025/12/4 15:28
 */
@RequiredArgsConstructor
@Getter
public enum UserRoleEnum {

    USER(0, "user"),
    ADMIN(1, "admin");

    /**
     * 角色值
     */
    private final int value;
    /**
     * 角色名称
     */
    private final String name;

    public static UserRoleEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserRoleEnum role : values()) {
            if (role.getValue() == value) {
                return role;
            }
        }
        return null;
    }
}
