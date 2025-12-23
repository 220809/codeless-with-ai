package com.dingzk.codeless.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ToolManager {

    private final Map<String, BaseTool> nameToolMap = new HashMap<>();

    @Resource
    private BaseTool[] tools;

    @PostConstruct
    public void init() {
        for (BaseTool tool : tools) {
            nameToolMap.put(tool.getName(), tool);
        }
    }

    public BaseTool getTool(String name) {
        return nameToolMap.get(name);
    }

    public BaseTool[] getTools() {
        return tools;
    }
}
