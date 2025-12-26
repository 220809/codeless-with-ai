package com.dingzk.codeless;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Spring Boot 启动类
 *
 * @author ding
 * @date 2025/12/3 20:05
 */
@EnableCaching
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@MapperScan("com.dingzk.codeless.mapper")
public class CodelessApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodelessApplication.class, args);
    }

}
