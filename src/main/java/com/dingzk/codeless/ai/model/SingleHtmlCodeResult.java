package com.dingzk.codeless.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * 单个html代码结果
 *
 * @author ding
 * @date 2025/12/6 16:14
 */
@Description("生成单个html代码文件的结果")
@Data
public class SingleHtmlCodeResult {
    /**
     * html代码
     */
    @Description("生成的html代码")
    private String htmlCode;
    /**
     * 描述
     */
    @Description("生成的代码的描述")
    private String description;
}
