package com.dingzk.codeless.core;

import com.dingzk.codeless.ai.AiGenCodeService;
import com.dingzk.codeless.core.parser.CodeParserExecutor;
import com.dingzk.codeless.core.saver.CodeSaverExecutor;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
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

    @Resource
    private AiGenCodeService aiGenCodeService;

    /**
     * 统一调用: 生成代码并保存到本地
     * @param userMessage 用户输入的描述信息
     * @param fileType 生成文件类型
     * @return 生成的代码文件
     */
    public File generateAndSaveCodeFile(String userMessage, GenFileTypeEnum fileType) {
        ThrowUtils.throwIf(fileType == null, ErrorCode.BAD_PARAM_ERROR, "生成文件类型不能为空");
        Object codeResult;
        switch (fileType) {
            case SINGLE_HTML -> codeResult = aiGenCodeService.genSingleHtmlCode(userMessage);
            case MULTI_FILE -> codeResult = aiGenCodeService.genMultiFileCode(userMessage);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码保存类型错误");
        }
        File file = CodeSaverExecutor.execute(codeResult, fileType);
        log.info("Successfully save code file, saved in: {}", file.getName());
        return file;
    }

    /**
     * 统一调用: 生成代码并保存到本地（流式）
     * @param userMessage 用户输入的描述信息
     * @param fileType 生成文件类型
     * @return 生成的代码文件
     */
    public Flux<String> streamingGenerateAndSaveCodeFile(String userMessage, GenFileTypeEnum fileType) {
        ThrowUtils.throwIf(fileType == null, ErrorCode.BAD_PARAM_ERROR, "生成文件类型不能为空");
        Flux<String> streamingResult;
        switch (fileType) {
            case SINGLE_HTML -> streamingResult = aiGenCodeService.streamingGenSingleHtmlCode(userMessage);
            case MULTI_FILE -> streamingResult = aiGenCodeService.streamingGenMultiFileCode(userMessage);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码保存类型错误");
        }
        return streamingGenerateAndSaveCode(streamingResult, fileType);
    }

    /**
     * 统一调用: 生成代码并保存到本地（流式）
     * @param streamingResult 流式生成结果
     * @param genFileType 生成文件类型
     * @return 生成的代码文件
     */
    private Flux<String> streamingGenerateAndSaveCode(Flux<String> streamingResult, GenFileTypeEnum genFileType) {
        StringBuilder partialAiMessage = new StringBuilder();
        return streamingResult
                .doOnNext(partialAiMessage::append)
                .doOnComplete(() -> {
                    try {
                        String completedAiMessage = partialAiMessage.toString();
                        // 解析ai生成内容
                        Object codeResult = CodeParserExecutor.execute(completedAiMessage, genFileType);
                        // 保存代码文件
                        File file = CodeSaverExecutor.execute(codeResult, genFileType);
                        log.info("Successfully save code files from stream result, saved in: {}", file.getName());
                    } catch (Exception e) {
                        log.error("Failed to save code files, message: {}", e.getMessage());
                    }
                });
    }
}
