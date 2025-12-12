package com.dingzk.codeless.core;

import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiGenCodeFacadeTest {

    @Resource
    private AiGenCodeFacade aiGenCodeFacade;

    private static final Long TEST_APP_ID = 1L;

    @Test
    void generateAndSaveCodeFileTest() {
        File file = aiGenCodeFacade
                .generateAndSaveCodeFile("帮我生成一个登录页面, 代码限制在50行以内",
                        GenFileTypeEnum.MULTI_FILE, TEST_APP_ID);
        Assertions.assertNotNull(file);
    }

    @Test
    void streamingGenerateAndSaveCodeFileTest() {
        Flux<String> fluxResult = aiGenCodeFacade
                .streamingGenerateAndSaveCodeFile("帮我生成一个简易计算器工具, 代码限制在20行以内",
                        GenFileTypeEnum.MULTI_FILE, TEST_APP_ID);
        List<String> stringList = fluxResult.collectList().block();
        assertNotNull(stringList);
        String stringResult = String.join("", stringList);
        assertNotNull(stringResult);
    }

    @Test
    void streamingGenerateAndSaveCodeFileTest_ChatMemory() {
        Flux<String> fluxResult = aiGenCodeFacade
                .streamingGenerateAndSaveCodeFile("帮我生成一个简易计算器工具, 代码限制在20行以内.",
                        GenFileTypeEnum.MULTI_FILE, TEST_APP_ID);
        // 等待 ai 回复结果
        List<String> stringList = fluxResult.collectList().block();
        assertNotNull(stringList);
        fluxResult = aiGenCodeFacade
                .streamingGenerateAndSaveCodeFile("接下来你不需要生成代码，请告诉我你刚刚做了什么?",
                        GenFileTypeEnum.MULTI_FILE, TEST_APP_ID);
        // 等待 ai 回复结果
        stringList = fluxResult.collectList().block();
        assertNotNull(stringList);
    }
}