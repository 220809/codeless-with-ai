package com.dingzk.codeless.ai.tools;

import com.dingzk.codeless.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * FileWriteTool
 *
 * @author ding
 * @date 2025/12/13 15:24
 */
@Slf4j
public class FileWriteTool {
    @Tool("write content to a file at the specified relative path")
    public String writeFile(@P("the relative path of the file to write") String relativePath,
                            @P("the file content to write") String content,
                            @ToolMemoryId long appId) {
        try {
            Path path = Paths.get(relativePath);
            if (!path.isAbsolute()) {
                // 相对路径处理，创建基于 appId 的项目目录
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_FILE_SAVE_BASE_PATH, projectDirName);
                path = projectRoot.resolve(relativePath);
            }
            // 创建父目录（如果不存在）
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            // 写入文件内容
            Files.write(path, content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            log.info("成功写入文件: {}", path.toAbsolutePath());
            // 注意要返回相对路径，不能让 AI 把文件绝对路径返回给用户
            return "文件写入成功: " + relativePath;
        } catch (IOException e) {
            String errorMessage = "文件写入失败: " + relativePath + ", 错误: " + e.getMessage();
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}
