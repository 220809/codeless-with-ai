package com.dingzk.codeless.ai.model.message;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * ai回复消息
 *
 * @author ding
 * @date 2025/12/14 14:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class AiResponseMessage extends StreamMessage {

    private String content;

    public AiResponseMessage(String content) {
        super(StreamMessageTypeEnum.AI_RESPONSE.getValue());
        this.content = content;
    }
}
