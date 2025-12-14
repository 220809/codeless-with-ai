package com.dingzk.codeless.core.handler;

import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.enums.ChatMessageTypeEnum;
import com.dingzk.codeless.service.ChatHistoryService;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;

/**
 * 处理纯文本消息（single_html、multi_file模式）
 *
 * @author ding
 * @date 2025/12/14 15:20
 */
public class PlainTextMessageHandler {

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
            aiMessageBuilder.append(chunk);
            return chunk;
        }).doOnComplete(() -> {
            String aiMessage = aiMessageBuilder.toString();
            if (StringUtils.isNotBlank(aiMessage)) {
                chatHistoryService.addChatHistory(appId, aiMessage, ChatMessageTypeEnum.AI.getType(), loginUser);
            }
        }).doOnError(error -> {
            // 错误时保存异常消息
            String errorMessage = StringUtils.join("生成代码失败: ", error.getMessage());
            chatHistoryService.addChatHistory(appId, errorMessage, ChatMessageTypeEnum.AI.getType(), loginUser);
        });
    }
}
