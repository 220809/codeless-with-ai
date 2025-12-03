package com.dingzk.codeless.controller;

import com.dingzk.codeless.common.BaseResponse;
import com.dingzk.codeless.common.RespUtils;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查接口, 用于测试 Knife4J 文档是否正常
 * 使用 @Hidden 可以忽略对此 Controller 的扫描
 *
 * @author ding
 * @date 2025/12/3 19:55
 */
@RestController
@RequestMapping("/health")
@Hidden
public class HealthController {
    /**
     * 健康检查接口(测试用)
     * @return "OK"
     */
    @GetMapping
    public BaseResponse<String> healthCheck() {
        return RespUtils.success("OK");
    }
}
