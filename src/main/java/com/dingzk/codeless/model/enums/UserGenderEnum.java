package com.dingzk.codeless.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 用户性别枚举
 *
 * @author ding
 * @date 2025/12/4 15:34
 */
@RequiredArgsConstructor
@Getter
public enum UserGenderEnum {

    UNKNOWN(0, "unknown"),
    MALE(1, "male"),
    FEMALE(2, "female");

    /**
     * 性别值
     */
    private final int value;
    /**
     * 性别名称
     */
    private final String name;

    public static UserGenderEnum fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserGenderEnum gender : UserGenderEnum.values()) {
            if (gender.getValue() == value) {
                return gender;
            }
        }
        return null;
    }
}
