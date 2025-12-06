package com.dingzk.codeless.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 多文件代码结果
 *
 * @author ding
 * @date 2025/12/6 16:14
 */
@Description("生成的多文件代码的结果")
@Data
public class MultiFileCodeResult {
    /**
     * html代码
     */
    @Description("生成的html代码")
    private String htmlCode;
    /**
     * css代码
     */
    @Description("生成的css代码")
    private String cssCode;
    /**
     * js代码
     */
    @Description("生成的js代码")
    private String jsCode;
    /**
     * 描述
     */
    @Description("生成的代码的描述")
    private String description;
}
