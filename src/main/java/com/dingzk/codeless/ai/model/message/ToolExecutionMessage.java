package com.dingzk.codeless.ai.model.message;

import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具执行消息
 *
 * @author ding
 * @date 2025/12/14 14:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ToolExecutionMessage extends StreamMessage {

    /**
     * 工具名称
     */
    private String name;
    /**
     * 工具参数
     */
    private String arguments;
    /**
     * 工具执行结果
     */
    private String result;

    public ToolExecutionMessage(ToolExecution toolExecution) {
        super(StreamMessageTypeEnum.TOOL_EXECUTION.getValue());
        this.name = toolExecution.request().name();
        this.arguments = toolExecution.request().arguments();
        this.result = toolExecution.result();
    }
}
