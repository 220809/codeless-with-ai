package com.dingzk.codeless.core;

import cn.hutool.json.JSONUtil;
import com.dingzk.codeless.ai.AiGenCodeService;
import com.dingzk.codeless.ai.AiGenCodeServiceFactory;
import com.dingzk.codeless.ai.model.message.AiResponseMessage;
import com.dingzk.codeless.ai.model.message.ToolExecutionMessage;
import com.dingzk.codeless.ai.model.message.ToolRequestMessage;
import com.dingzk.codeless.core.parser.CodeParserExecutor;
import com.dingzk.codeless.core.saver.CodeSaverExecutor;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * 代码生成门面
 *
 * @author ding
 * @date 2025/12/6 18:44
 */
@Slf4j
@Component
public class AiGenCodeFacade {

    // @Lazy 避免测试 bean 干扰项目，此 Bean 由 AiGenCodeServiceFactory.aiGenCodeService 创建
    @Resource
    @Lazy
    private AiGenCodeService aiGenCodeService;

    @Resource
    private AiGenCodeServiceFactory aiGenCodeServiceFactory;

    /**
     * 统一调用: 生成代码并保存到本地
     * @param userMessage 用户输入的描述信息
     * @param fileType 生成文件类型
     * @return 生成的代码文件
     */
    public File generateAndSaveCodeFile(String userMessage, GenFileTypeEnum fileType, Long appId) {
        ThrowUtils.throwIf(fileType == null, ErrorCode.BAD_PARAM_ERROR, "生成文件类型不能为空");
        Object codeResult;
        switch (fileType) {
            case SINGLE_HTML -> codeResult = aiGenCodeService.genSingleHtmlCode(userMessage);
            case MULTI_FILE -> codeResult = aiGenCodeService.genMultiFileCode(userMessage);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码保存类型错误");
        }
        File file = CodeSaverExecutor.execute(codeResult, fileType, appId);
        log.info("Successfully save code file, saved in: {}", file.getName());
        return file;
    }

    /**
     * 统一调用: 生成代码并保存到本地（流式）
     * @param userMessage 用户输入的描述信息
     * @param fileType 生成文件类型
     * @return 生成的代码文件
     */
    public Flux<String> streamingGenerateAndSaveCodeFile(String userMessage, GenFileTypeEnum fileType, Long appId) {
        ThrowUtils.throwIf(fileType == null, ErrorCode.BAD_PARAM_ERROR, "生成文件类型不能为空");
        Flux<String> streamingResult;
        boolean useReasonerModel = fileType.equals(GenFileTypeEnum.VUE_PROJECT);
        AiGenCodeService genCodeService = aiGenCodeServiceFactory.getAiGenCodeService(appId, useReasonerModel);
        switch (fileType) {
            case SINGLE_HTML -> streamingResult = genCodeService.streamingGenSingleHtmlCode(userMessage);
            case MULTI_FILE -> streamingResult = genCodeService.streamingGenMultiFileCode(userMessage);
            case VUE_PROJECT -> streamingResult =
                    processTokenStreamingResult(genCodeService.streamingGenVueProjectCode(appId, userMessage));
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码保存类型错误");
        }
        return streamingGenerateAndSaveCode(streamingResult, fileType, appId);
    }

    /**
     * 统一调用: 生成代码并保存到本地（流式）
     * @param streamingResult 流式生成结果
     * @param genFileType 生成文件类型
     * @return 生成的代码文件
     */
    private Flux<String> streamingGenerateAndSaveCode(Flux<String> streamingResult, GenFileTypeEnum genFileType, Long appId) {
        StringBuilder partialAiMessage = new StringBuilder();
        return streamingResult
                .doOnNext(partialAiMessage::append)
                .doOnComplete(() -> {
                    try {
                        String completedAiMessage = partialAiMessage.toString();
                        // 解析ai生成内容
                        Object codeResult = CodeParserExecutor.execute(completedAiMessage, genFileType);
                        // 保存代码文件
                        File file = CodeSaverExecutor.execute(codeResult, genFileType, appId);
                        log.info("Successfully save code files from stream result, saved in: {}", file.getName());
                    } catch (Exception e) {
                        log.error("Failed to save code files, message: {}", e.getMessage());
                    }
                });
    }

    /**
     * 处理 TokenStream 结果: 将流式结果包装为指定的 Json 数据, 并转换为 flux
     * @param tokenStream tokenStream
     * @return flux
     */
    private Flux<String> processTokenStreamingResult(TokenStream tokenStream) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse(partialResponse -> {
                // 包装 ai 流式消息
                AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                sink.next(JSONUtil.toJsonStr(aiResponseMessage));
            }).beforeToolExecution(beforeToolExecution -> {
                // 包装工具执行前消息
                ToolRequestMessage toolRequestMessage = new ToolRequestMessage(beforeToolExecution);
                sink.next(JSONUtil.toJsonStr(toolRequestMessage));
            }).onToolExecuted(toolExecution -> {
                // 包装工具执行后消息
                ToolExecutionMessage toolExecutionMessage = new ToolExecutionMessage(toolExecution);
                sink.next(JSONUtil.toJsonStr(toolExecutionMessage));
            }).onCompleteResponse(completeResponse -> {
                // 流结束
                sink.complete();
            }).onError(error -> {
                // 错误处理
                log.error("处理 TokenStream 遇到了错误: {}", error.getMessage());
                sink.error(error);
            }).start();
        });
    }
}
