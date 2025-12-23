package com.dingzk.codeless.ai.tools;

import com.dingzk.codeless.constant.AppConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FileReadTool - 读取文件内容工具
 *
 * @author ding
 * @date 2025/12/13
 */
@Slf4j
public class FileReadTool {
    
    /**
     * 读取文件内容
     *
     * @param relativePath 文件的相对路径
     * @param appId 应用ID，用于确定项目目录
     * @return 文件内容或错误信息
     */
    @Tool("read content from a file at the specified relative path")
    public String readFile(@P("the relative path of the file to read") String relativePath,
                          @ToolMemoryId long appId) {
        try {
            // 安全路径处理
            String safeRelativePath = FileToolUtil.resolveSafePath(relativePath);
            if (safeRelativePath == null) {
                return String.format("文件读取失败: 路径不安全或超出允许范围 - %s", relativePath);
            }
            
            // 构建项目根目录路径
            String projectDirName = "vue_project_" + appId;
            Path projectRoot = Paths.get(AppConstant.CODE_FILE_SAVE_BASE_PATH, projectDirName);
            
            // 解析最终路径
            Path path = projectRoot.resolve(safeRelativePath).normalize();
            
            // 验证解析后的路径是否在项目根目录内（防止路径遍历攻击）
            if (!path.startsWith(projectRoot)) {
                return String.format("文件读取失败: 路径不安全或超出允许范围 - %s", relativePath);
            }
            
            // 检查文件是否存在
            if (!Files.exists(path)) {
                return String.format("文件读取失败: 文件不存在 - %s", relativePath);
            }
            
            // 检查是否为文件（不是目录）
            if (!Files.isRegularFile(path)) {
                return String.format("文件读取失败: 路径指向的不是文件 - %s", relativePath);
            }
            
            // 读取文件内容
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes, StandardCharsets.UTF_8);
            log.info("成功读取文件: {}", path.toAbsolutePath());
            return content;
        } catch (IOException e) {
            String errorMessage = String.format("文件读取失败: %s, 错误: %s", relativePath, e.getMessage());
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
}

