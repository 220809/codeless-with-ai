package com.dingzk.codeless.core;

import com.dingzk.codeless.ai.model.MultiFileCodeResult;
import com.dingzk.codeless.ai.model.SingleHtmlCodeResult;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 保存代码文件
 *
 * @author ding
 * @date 2025/12/6 18:44
 */
@Slf4j
public class SaveCodeFileHelper {

    /**
     * 代码文件保存根路径
     */
    private static final String CODE_FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";

    public static File saveSingleHtmlCodeResult(SingleHtmlCodeResult singleHtmlCodeResult) {
        String fileBaseDir = createUniqueFileDir(GenFileTypeEnum.SINGLE_HTML.getName());
        saveCodeFile(fileBaseDir, "index.html", singleHtmlCodeResult.getHtmlCode());
        return new File(fileBaseDir);
    }

    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult) {
        String fileBaseDir = createUniqueFileDir(GenFileTypeEnum.MULTI_FILE.getName());
        saveCodeFile(fileBaseDir, "index.html", multiFileCodeResult.getHtmlCode());
        saveCodeFile(fileBaseDir, "style.css", multiFileCodeResult.getCssCode());
        saveCodeFile(fileBaseDir, "script.js", multiFileCodeResult.getJsCode());
        return new File(fileBaseDir);
    }

    /**
     * 生成唯一文件目录
     * @param bizType 业务类型
     * @return 文件目录路径
     */
    private static String createUniqueFileDir(String bizType) {
        String dirName = StringUtils.join(bizType, "_", RandomStringUtils.secure().nextAlphanumeric(16));
        String dirPath = StringUtils.join(CODE_FILE_SAVE_ROOT_PATH, File.separator, dirName);
        try {
            FileUtils.forceMkdir(new File(dirPath));
        } catch (IOException e) {
            log.error("create unique file dir failed, message: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建代码文件目录失败");
        }
        return dirPath;
    }

    public static void saveCodeFile(String dirPath, String fileName, String fileContent) {
        try {
            FileUtils.writeStringToFile(new File(StringUtils.join(dirPath, File.separator, fileName)), fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("save code file failed, message: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存代码文件失败");
        }
    }
}
