package com.dingzk.codeless.ai;

import com.dingzk.codeless.ai.tools.*;
import com.dingzk.codeless.service.ChatHistoryService;
import com.dingzk.codeless.utils.SpringContextUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * AiGenCodeServiceFactory
 *
 * @author ding
 * @date 2025/12/6 15:56
 */
@Configuration
@Slf4j
public class AiGenCodeServiceFactory {

    @Resource
    private ChatModel openAiChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private SpringContextUtil springContextUtil;

    @Resource
    private ToolManager toolManager;

    private static final int MAX_MESSAGE_COUNT = 40;

    /**
     * aiService 本地缓存
     */
    private final Cache<Long, AiGenCodeService> aiServiceCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 获取 AiGenCodeService（通用）
     * @param appId appId
     * @return AiGenCodeService
     */
    public AiGenCodeService getAiGenCodeService(long appId, boolean useReasonerModel) {
        return aiServiceCache.get(appId, id -> createAiGenCodeService(id, useReasonerModel));
    }

    /**
     * 创建 AiGenCodeService（通用）
     * @param appId appId
     * @return AiGenCodeService
     */
    private AiGenCodeService createAiGenCodeService(long appId, boolean useReasonerModel) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)  // 使用 appId 作为对话记忆id, 在应用层面隔离
                .chatMemoryStore(redisChatMemoryStore) // 对话记忆存储
                .maxMessages(MAX_MESSAGE_COUNT)  // 对话记忆最大条数
                .build();
        // 加载对话历史
        chatHistoryService.loadChatHistoryToChatMemory(appId, chatMemory, MAX_MESSAGE_COUNT);

        if (useReasonerModel) {
            // 获取 prototype bean
            StreamingChatModel streamingReasonerChatModel =
                    springContextUtil.getBean("protoStreamingReasonerChatModel", StreamingChatModel.class);
            // 使用推理模型，并使用工具调用
            return AiServices.builder(AiGenCodeService.class)
                    .chatModel(openAiChatModel)
                    .streamingChatModel(streamingReasonerChatModel)
                    .chatMemoryProvider(memoryId -> chatMemory)
                    // 定义工具
                    .tools(toolManager.getTools())
                    // 模型幻觉处理（调用不存在的工具时）
                    .hallucinatedToolNameStrategy(toolExecutionRequest -> ToolExecutionResultMessage.from(
                            toolExecutionRequest, String.format("Error: tool[%s] not found", toolExecutionRequest.name())
                    )).build();
        }

        // 获取 prototype bean
        StreamingChatModel streamingChatModel =
                springContextUtil.getBean("protoStreamingChatModel", StreamingChatModel.class);
        // 使用非推理模型
        return AiServices.builder(AiGenCodeService.class)
                .chatModel(openAiChatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }


    /**
     * 测试调用 ai 使用，使用位置在 com.dingzk.codeless.ai.AiGenCodeServiceTest
     * 需要用到时取消 @Bean 注释
     * @return 用于测试的 aiService
     */
//    @Bean
    public AiGenCodeService aiGenCodeService() {
        return createAiGenCodeService(1L, false);
    }

    /**
     * ai 决策文件类型模型
     * @return service
     */
    @Bean
    public AiGenFileTypeRoutingService aiGenFileTypeRoutingService() {
        return AiServices.builder(AiGenFileTypeRoutingService.class)
                .chatModel(openAiChatModel)
                .build();
    }
}
