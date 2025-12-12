package com.dingzk.codeless.ai;

import com.dingzk.codeless.service.ChatHistoryService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
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
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Resource
    private ChatHistoryService chatHistoryService;

    private static final int MAX_MESSAGE_COUNT = 20;

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

    public AiGenCodeService getAiGenCodeService(long appId) {
        return aiServiceCache.get(appId, this::createAiGenCodeService);
    }

    private AiGenCodeService createAiGenCodeService(long appId) {
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .id(appId)  // 使用 appId 作为对话记忆id, 在应用层面隔离
                .chatMemoryStore(redisChatMemoryStore) // 对话记忆存储
                .maxMessages(MAX_MESSAGE_COUNT)  // 对话记忆最大条数
                .build();
        // 加载对话历史
        chatHistoryService.loadChatHistoryToChatMemory(appId, chatMemory, MAX_MESSAGE_COUNT);
        return AiServices.builder(AiGenCodeService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                .build();
    }

    @Bean
    public AiGenCodeService aiGenCodeService() {
        return AiServices.builder(AiGenCodeService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .build();
    }
}
