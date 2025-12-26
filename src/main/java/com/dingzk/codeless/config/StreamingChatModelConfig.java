package com.dingzk.codeless.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * 流式模型配置类
 *
 * @author ding
 * @date 2025/12/26 15:57
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.openai.streaming-chat-model")
@Data
public class StreamingChatModelConfig {
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
    /**
     * 是否打印请求
     */
    private boolean logRequests;
    /**
     * 是否打印响应
     */
    private boolean logResponses;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public StreamingChatModel protoStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .build();
    }
}
