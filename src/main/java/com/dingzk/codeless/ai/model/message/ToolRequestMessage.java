package com.dingzk.codeless.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.service.tool.BeforeToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具请求消息
 *
 * @author ding
 * @date 2025/12/14 14:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ToolRequestMessage extends StreamMessage {

    /**
     * 工具名称
     */
    private String name;

    public ToolRequestMessage(BeforeToolExecution beforeToolExecution) {
        super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
        this.name = beforeToolExecution.request().name();
    }
}
