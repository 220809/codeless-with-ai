package com.dingzk.codeless.ai;

import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
class AiGenFileTypeRoutingServiceTest {

    @Resource
    private AiGenFileTypeRoutingService aiGenFileTypeRoutingService;

    @Test
    void routeGenFileTypeTest() {
        String userPrompt = "做一个简单的个人介绍页面";
        GenFileTypeEnum result = aiGenFileTypeRoutingService.routeGenFileType(userPrompt);
        log.info("用户需求: {} -> {}", userPrompt, result.getName());
        userPrompt = "做一个公司官网，需要首页、关于我们、联系我们三个页面";
        result = aiGenFileTypeRoutingService.routeGenFileType(userPrompt);
        log.info("用户需求: {} -> {}", userPrompt, result.getName());
        userPrompt = "做一个电商管理系统，包含用户管理、商品管理、订单管理，需要路由和状态管理";
        result = aiGenFileTypeRoutingService.routeGenFileType(userPrompt);
        log.info("用户需求: {} -> {}", userPrompt, result.getName());
    }
}