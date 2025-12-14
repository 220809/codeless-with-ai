package com.dingzk.codeless.ai.model.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息基类
 *
 * @author ding
 * @date 2025/12/14 14:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {
    /**
     * 消息类型
     */
    private String type;
}
