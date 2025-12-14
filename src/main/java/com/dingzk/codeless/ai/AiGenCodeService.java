package com.dingzk.codeless.ai;

import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;

/**
 * ai生成代码服务
 *
 * @author ding
 * @date 2025/12/6 15:39
 */
public interface AiGenCodeService {

    /**
     * 生成单文件html代码
     * @param userMessage 用户输入
     * @return AI返回内容
     */
    @SystemMessage(fromResource = "prompts/gencode_single_html_prompt.txt")
    SingleHtmlCodeResult genSingleHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage 用户输入
     * @return AI返回内容
     */
    @SystemMessage(fromResource = "prompts/gencode_multi_file_prompt.txt")
    MultiFileCodeResult genMultiFileCode(String userMessage);

    /**
     * 生成单文件html代码（流式输出）
     * @param userMessage 用户输入
     * @return AI返回内容
     */
    @SystemMessage(fromResource = "prompts/gencode_single_html_prompt.txt")
    Flux<String> streamingGenSingleHtmlCode(String userMessage);

    /**
     * 生成多文件代码（流式输出）
     * @param userMessage 用户输入
     * @return AI返回内容
     */
    @SystemMessage(fromResource = "prompts/gencode_multi_file_prompt.txt")
    Flux<String> streamingGenMultiFileCode(String userMessage);

    /**
     * 生成Vue项目代码（流式输出）
     * @param userMessage 用户输入
     * @return AI返回内容
     */
    @SystemMessage(fromResource = "prompts/gencode_vue_project_prompt.txt")
    TokenStream streamingGenVueProjectCode(@MemoryId long appId, @UserMessage String userMessage);
}
