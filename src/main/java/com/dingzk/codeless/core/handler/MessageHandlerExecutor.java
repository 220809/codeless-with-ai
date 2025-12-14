package com.dingzk.codeless.core.handler;

import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import com.dingzk.codeless.service.ChatHistoryService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * 消息处理执行器
 *
 * @author ding
 * @date 2025/12/14 15:47
 */
@Component
public class MessageHandlerExecutor {
    @Resource
    private JsonDataMessageHandler jsonDataMessageHandler;

    /**
     * 根据文件类型执行消息处理
     * @param aiMessageFlux AI消息流
     * @param chatHistoryService 聊天历史 service
     * @param appId 应用ID
     * @param loginUser 登录用户
     * @param genFileType 生成文件类型
     * @return flux
     */
    public Flux<String> execute(Flux<String> aiMessageFlux, ChatHistoryService chatHistoryService,
                        long appId, User loginUser, GenFileTypeEnum genFileType) {
        return switch (genFileType) {
            case VUE_PROJECT ->
                    jsonDataMessageHandler.handle(aiMessageFlux, chatHistoryService, appId, loginUser);
            case SINGLE_HTML, MULTI_FILE ->
                    new PlainTextMessageHandler().handle(aiMessageFlux, chatHistoryService, appId, loginUser);
        };
    }
}
