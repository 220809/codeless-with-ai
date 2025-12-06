package com.dingzk.codeless.core.saver;

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
 * 代码保存器抽象类
 *
 * @author ding
 * @date 2025/12/6 22:01
 */
@Slf4j
public abstract class AbstractCodeSaver<T> {
    /**
     * 代码文件保存根路径
     */
    private static final String CODE_FILE_SAVE_ROOT_PATH = System.getProperty("user.dir") + "/tmp/code_output";
    public final File save(T codeResult) {
        // 1. 校验参数
        validateBeforeSave(codeResult);
        // 2. 生成保存代码根目录
        String fileBaseDir = createUniqueFileDir();
        // 3. 保存代码文件
        saveFiles(fileBaseDir, codeResult);
        // 4. 返回保存的文件目录
        return new File(fileBaseDir);
    }

    /**
     * 生成唯一文件目录
     * @return 文件目录路径
     */
    private String createUniqueFileDir() {
        String bizType = genFileTypeEnum().getName();
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

    protected void validateBeforeSave(T codeResult) {
        if (codeResult == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "保存代码参数为空");
        }
    }

    protected abstract void saveFiles(String fileBaseDir, T codeResult);

    protected abstract GenFileTypeEnum genFileTypeEnum();

    protected static void saveCodeFile(String dirPath, String fileName, String fileContent) {
        try {
            FileUtils.writeStringToFile(new File(StringUtils.join(dirPath, File.separator, fileName)), fileContent, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("save code file failed, message: {}", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存代码文件失败");
        }
    }
}
