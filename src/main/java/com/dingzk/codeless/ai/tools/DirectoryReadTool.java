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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * DirectoryReadTool - 读取目录结构工具
 *
 * @author ding
 * @date 2025/12/13
 */
@Slf4j
public class DirectoryReadTool {
    
    /**
     * 读取目录结构
     *
     * @param relativePath 目录的相对路径，如果为空或"."则读取项目根目录
     * @param appId 应用ID，用于确定项目目录
     * @return 目录结构信息（树形结构字符串）或错误信息
     */
    @Tool("read directory structure at the specified relative path, returns a tree-like structure of files and directories")
    public String readDirectory(@P("the relative path of the directory to read, use '.' or empty string for project root") String relativePath,
                               @ToolMemoryId long appId) {
        try {
            // 构建项目根目录路径
            String projectDirName = "vue_project_" + appId;
            Path projectRoot = Paths.get(AppConstant.CODE_FILE_SAVE_BASE_PATH, projectDirName);
            
            Path path;
            // 空路径或"."表示项目根目录
            if (relativePath == null || relativePath.isEmpty() || relativePath.equals(".")) {
                path = projectRoot;
            } else {
                // 安全路径处理
                String safeRelativePath = FileToolUtil.resolveSafePath(relativePath);
                if (safeRelativePath == null) {
                    return String.format("目录读取失败: 路径不安全或超出允许范围 - %s", relativePath);
                }
                
                // 解析最终路径
                path = projectRoot.resolve(safeRelativePath).normalize();
                
                // 验证解析后的路径是否在项目根目录内（防止路径遍历攻击）
                if (!path.startsWith(projectRoot)) {
                    return String.format("目录读取失败: 路径不安全或超出允许范围 - %s", relativePath);
                }
            }
            
            // 检查路径是否存在
            if (!Files.exists(path)) {
                return String.format("目录读取失败: 路径不存在 - %s", relativePath);
            }
            
            // 检查是否为目录
            if (!Files.isDirectory(path)) {
                return String.format("目录读取失败: 路径指向的不是目录 - %s", relativePath);
            }
            
            // 构建目录树
            StringBuilder tree = new StringBuilder();
            // 计算相对路径前缀（用于显示）
            String displayPrefix = path.equals(projectRoot) ? "" : 
                projectRoot.relativize(path).toString().replace("\\", "/") + "/";
            
            buildDirectoryTree(path, displayPrefix, tree, "");
            
            log.info("成功读取目录: {}", path.toAbsolutePath());
            return tree.toString();
        } catch (IOException e) {
            String errorMessage = String.format("目录读取失败: %s, 错误: %s", relativePath, e.getMessage());
            log.error(errorMessage, e);
            return errorMessage;
        }
    }
    
    /**
     * 递归构建目录树结构
     *
     * @param dir 当前目录
     * @param displayPrefix 显示前缀
     * @param tree 树形结构字符串构建器
     * @param indent 当前缩进
     */
    private void buildDirectoryTree(Path dir, String displayPrefix,
                                    StringBuilder tree, String indent) throws IOException {
        // 添加当前目录名称
        String dirName = dir.getFileName() != null ? dir.getFileName().toString() : displayPrefix;
        tree.append(indent).append(dirName).append("/\n");
        
        // 更新缩进（仅使用空格）
        String newIndent = indent + "    ";
        
        // 获取目录下的所有条目
        List<Path> entries = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.sorted((p1, p2) -> {
                // 目录优先，然后按名称排序
                boolean d1 = Files.isDirectory(p1);
                boolean d2 = Files.isDirectory(p2);
                if (d1 != d2) {
                    return d1 ? -1 : 1;
                }
                return p1.getFileName().toString().compareToIgnoreCase(p2.getFileName().toString());
            }).forEach(entries::add);
        }
        
        // 递归处理每个条目
        for (Path entry : entries) {
            if (Files.isDirectory(entry)) {
                // 递归处理子目录
                buildDirectoryTree(entry, displayPrefix, tree, newIndent);
            } else {
                // 添加文件
                String fileName = entry.getFileName().toString();
                tree.append(newIndent).append(fileName).append("\n");
            }
        }
    }
}