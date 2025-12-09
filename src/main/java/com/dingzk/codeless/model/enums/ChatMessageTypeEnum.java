package com.dingzk.codeless.model.enums;

import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 对话消息类型枚举
 *
 * @author ding
 * @date 2025/12/9 22:04
 */
@Getter
@RequiredArgsConstructor
public enum ChatMessageTypeEnum {
    USER("user", "用户消息"),
    AI("ai", "AI消息");

    /**
     * 对话消息类型
     */
    private final String type;
    /**
     * 对话消息描述
     */
    private final String text;

    public static ChatMessageTypeEnum getByType(String type) {
        if (StringUtils.isEmpty(type)) {
            return null;
        }
        for (ChatMessageTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        return null;
    }
}
