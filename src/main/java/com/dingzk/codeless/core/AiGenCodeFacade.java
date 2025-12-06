package com.dingzk.codeless.core;

import com.dingzk.codeless.ai.AiGenCodeService;
import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
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
        return switch (fileType) {
            case SINGLE_HTML -> generateAndSaveSingleHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
        };
    }

    /**
     * 生成并保存单文件html代码
     * @param userMessage 用户输入的描述信息
     * @return 保存的目录
     */
    private File generateAndSaveSingleHtmlCode(String userMessage) {
        SingleHtmlCodeResult singleHtmlCodeResult = aiGenCodeService.genSingleHtmlCode(userMessage);
        File file = SaveCodeFileHelper.saveSingleHtmlCodeResult(singleHtmlCodeResult);
        log.info("Single html code file saved, saved in: {}", file.getName());
        return file;
    }

    /**
     * 生成并保存多文件代码
     * @param userMessage 用户输入的描述信息
     * @return 保存的目录
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult multiFileCodeResult = aiGenCodeService.genMultiFileCode(userMessage);
        File file = SaveCodeFileHelper.saveMultiFileCodeResult(multiFileCodeResult);
        log.info("Multi code file file saved, saved in: {}", file.getName());
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
        return switch (fileType) {
            case SINGLE_HTML -> streamingGenerateAndSaveSingleHtmlCode(userMessage);
            case MULTI_FILE -> streamingGenerateAndSaveMultiFileCode(userMessage);
        };
    }

    /**
     * 生成并保存单文件html代码（流式）
     * @param userMessage 用户输入的描述信息
     * @return 保存的目录
     */
    private Flux<String> streamingGenerateAndSaveSingleHtmlCode(String userMessage) {
        Flux<String> streamingResult = aiGenCodeService.streamingGenSingleHtmlCode(userMessage);
        StringBuilder partialAiMessage = new StringBuilder();
        return streamingResult
                .doOnNext(chunk -> {
                    // 拼接 ai 消息
                    partialAiMessage.append(chunk);
                })
                .doOnComplete(() -> {
                    try {
                        String completedAiMessage = partialAiMessage.toString();
                        SingleHtmlCodeResult singleHtmlCodeResult = AiMessageParser.parseSingleHtmlCode(completedAiMessage);
                        File file = SaveCodeFileHelper.saveSingleHtmlCodeResult(singleHtmlCodeResult);
                        log.info("Single html code file saved from streaming result, saved in: {}", file.getName());
                    } catch (Exception e) {
                        log.error("Failed to save single html code file from streaming result, message: {}", e.getMessage());
                    }
                });
    }

    /**
     * 生成并保存多文件代码（流式）
     * @param userMessage 用户输入的描述信息
     * @return 保存的目录
     */
    private Flux<String> streamingGenerateAndSaveMultiFileCode(String userMessage) {
        Flux<String> streamingResult = aiGenCodeService.streamingGenMultiFileCode(userMessage);
        StringBuilder partialAiMessage = new StringBuilder();
        return streamingResult
                .doOnNext(chunk -> {
                    // 拼接 ai 消息
                    partialAiMessage.append(chunk);
                })
                .doOnComplete(() -> {
                    try {
                        String completedAiMessage = partialAiMessage.toString();
                        MultiFileCodeResult multiFileCodeResult = AiMessageParser.parseMultiFileCode(completedAiMessage);
                        File file = SaveCodeFileHelper.saveMultiFileCodeResult(multiFileCodeResult);
                        log.info("Multi code file saved from streaming result, saved in: {}", file.getName());
                    } catch (Exception e) {
                        log.error("Failed to save multi code file from streaming result, message: {}", e.getMessage());
                    }
                });
    }
}
