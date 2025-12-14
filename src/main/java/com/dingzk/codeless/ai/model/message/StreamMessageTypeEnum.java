package com.dingzk.codeless.ai.model.message;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 消息类型枚举
 *
 * @author ding
 * @date 2025/12/14 18:17
 */
@RequiredArgsConstructor
@Getter
public enum StreamMessageTypeEnum {

    AI_RESPONSE("ai_response", "AI回复消息"),
    TOOL_REQUEST("tool_request", "工具请求消息"),
    TOOL_EXECUTION("tool_execution", "工具执行消息");

    /**
     * 消息类型值
     */
    private final String value;
    /**
     * 消息类型文本
     */
    private final String text;

    public static StreamMessageTypeEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (StreamMessageTypeEnum type : StreamMessageTypeEnum.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }
}
