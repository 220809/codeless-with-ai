package com.dingzk.codeless.core.handler;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dingzk.codeless.ai.model.message.*;
import com.dingzk.codeless.constant.AppConstant;
import com.dingzk.codeless.core.builder.VueProjectBuilder;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.enums.ChatMessageTypeEnum;
import com.dingzk.codeless.service.ChatHistoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * Json格式消息处理器
 *
 * @author ding
 * @date 2025/12/14 15:21
 */
@Component
@Slf4j
public class JsonDataMessageHandler {

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 消息处理
     * @param aiMessageFlux AI消息流
     * @param chatHistoryService 聊天历史 service
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @return flux
     */
    public Flux<String> handle(Flux<String> aiMessageFlux, ChatHistoryService chatHistoryService, long appId, User loginUser) {
        StringBuilder aiMessageBuilder = new StringBuilder();
        return aiMessageFlux.map(chunk -> {
            StreamMessage streamMessage = JSONUtil.toBean(chunk, StreamMessage.class);
            String type = streamMessage.getType();
            StreamMessageTypeEnum streamMessageType = StreamMessageTypeEnum.fromValue(type);
            switch (streamMessageType) {
                case AI_RESPONSE -> {
                    AiResponseMessage aiResponseMessage = JSONUtil.toBean(chunk, AiResponseMessage.class);
                    String content = aiResponseMessage.getContent();
                    aiMessageBuilder.append(content);
                    return content;
                }
                case TOOL_REQUEST -> {
                    ToolRequestMessage toolRequestMessage = JSONUtil.toBean(chunk, ToolRequestMessage.class);
                    String requestMessage = String.format("\n\n[调用工具] %s\n\n", toolRequestMessage.getName());
                    aiMessageBuilder.append(requestMessage);
                    return requestMessage;
                }
                case TOOL_EXECUTION -> {
                    ToolExecutionMessage toolExecutionMessage = JSONUtil.toBean(chunk, ToolExecutionMessage.class);
                    JSONObject toolArgumentObj = JSONUtil.parseObj(toolExecutionMessage.getArguments());
                    String relativePath = toolArgumentObj.getStr("relativePath");
                    String fileSuffix = FileUtil.getSuffix(relativePath);
                    String content = toolArgumentObj.getStr("content");
                    String executionMessage = String.format(
                            """
                            [工具执行结果] %s: %s
                            ```%s
                            %s
                            ```
                            """
                    , toolExecutionMessage.getName(), relativePath, fileSuffix, content);
                    String output = String.format("\n\n%s\n\n", executionMessage);
                    aiMessageBuilder.append(output);
                    return output;
                }
                default -> {
                    log.error("未知消息类型: {}", type);
                    return "";
                }
            }
        }).doOnComplete(() -> {
            String aiMessage = aiMessageBuilder.toString();
            if (StringUtils.isNotBlank(aiMessage)) {
                chatHistoryService.addChatHistory(appId, aiMessage, ChatMessageTypeEnum.AI.getType(), loginUser);
            }
            // 异步构建 Vue 项目
            String projectDir = AppConstant.CODE_FILE_SAVE_BASE_PATH + File.separator + "vue_project_" + appId;
            vueProjectBuilder.buildProjectAsync(projectDir);
        }).doOnError(error -> {
            // 错误时保存异常消息
            String errorMessage = StringUtils.join("生成代码失败: ", error.getMessage());
            chatHistoryService.addChatHistory(appId, errorMessage, ChatMessageTypeEnum.AI.getType(), loginUser);
        });
    }
}
