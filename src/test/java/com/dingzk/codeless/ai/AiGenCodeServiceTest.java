package com.dingzk.codeless.ai;

import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiGenCodeServiceTest {

    @Resource
    private AiGenCodeService aiGenCodeService;

    @Test
    void genSingleHtmlCodeTest() {
        SingleHtmlCodeResult result = aiGenCodeService.genSingleHtmlCode("帮我生成一个个人博客网站, 代码限制在50行以内");
        assertNotNull(result);
    }

    @Test
    void genMultiFileCodeTest() {
        MultiFileCodeResult result = aiGenCodeService.genMultiFileCode("帮我生成一个个人博客网站, 代码限制在120行以内");
        assertNotNull(result);
    }
}