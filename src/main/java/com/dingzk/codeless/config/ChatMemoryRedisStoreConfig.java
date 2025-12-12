package com.dingzk.codeless.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.community.store.memory.chat.redis.StoreType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话记忆 redis 存储配置类
 *
 * @author ding
 * @date 2025/12/10 15:39
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class ChatMemoryRedisStoreConfig {
    /**
     * redis host
     */
    private String host;
    /**
     * redis port
     */
    private int port;
    /**
     * redis password
     */
    private String password;
    /**
     * redis key ttl
     */
    private long ttl;

    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host)
                .port(port)
                .password(password)
                .storeType(StoreType.STRING)  // 默认是使用 RedisJson，可能需要特定的redis支持，现改为String
                .ttl(ttl)
                .build();
    }
}
