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

    @Test
    void generateAndSaveCodeFileTest() {
        File file = aiGenCodeFacade.generateAndSaveCodeFile("帮我生成一个登录页面, 代码限制在50行以内", GenFileTypeEnum.MULTI_FILE);
        Assertions.assertNotNull(file);
    }

    @Test
    void streamingGenerateAndSaveCodeFileTest() {
        Flux<String> fluxResult = aiGenCodeFacade.streamingGenerateAndSaveCodeFile("帮我生成一个注册页面, 代码限制在50行以内", GenFileTypeEnum.MULTI_FILE);
        List<String> stringList = fluxResult.collectList().block();
        assertNotNull(stringList);
        String stringResult = String.join("", stringList);
        assertNotNull(stringResult);
    }
}