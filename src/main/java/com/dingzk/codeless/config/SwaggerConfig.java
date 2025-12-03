package com.dingzk.codeless.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置
 * @author ding
 * @date 2025/12/3 19:43
 */
@Configuration
public class SwaggerConfig {
    /**
     * 接口文档主页描述
     * @return OPENAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CodeLess AI 应用生成平台 API")
                        .contact(new Contact()
                                .name("dingzk")
                                .url("https://github.com/220809")
                                .email("1575748757@1qq.com"))
                        .version("0.1")
                        .description("AI 零代码应用生成平台")
                        .termsOfService("https://github.com/220809"));
    }


}