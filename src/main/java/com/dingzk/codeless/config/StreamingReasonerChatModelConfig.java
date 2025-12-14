package com.dingzk.codeless.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 流式推理模型配置类
 *
 * @author ding
 * @date 2025/12/13 15:09
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.openai.chat-model")
@Data
public class StreamingReasonerChatModelConfig {
    /**
     * OpenAI API key
     */
    private String apiKey;
    /**
     * 模型名称
     */
    private String modelName;
    /**
     * 模型地址
     */
    private String baseUrl;
    /**
     * 最大token数
     */
    private int maxTokens;

    @Bean
    public StreamingChatModel streamingReasonerChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
