package com.dingzk.codeless.ai.tools;

import cn.hutool.json.JSONObject;

/**
 * 工具基类
 *
 * @author ding
 * @date 2025/12/23 19:34
 */
public abstract class BaseTool {
    /**
     * 工具名称
     * @return 工具名称
     */
    public abstract String getName();

    /**
     * 工具展示名称
     * @return 工具展示名称
     */
    public abstract String getDisplayName();

    /**
     * 工具请求结果
     * @return 工具请求结果
     */
    public String requestResult() {
        return String.format("\n\n[调用工具] %s\n\n", getDisplayName());
    }

    /**
     * 工具执行结果
     * @param args 工具参数
     * @return 工具执行结果
     */
    public String executionResult(JSONObject args) {
        String relativePath = args.getStr("relativePath");
        return String.format("[执行结果] %s: %s", getDisplayName(), relativePath);
    }
}
