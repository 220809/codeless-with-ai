package com.dingzk.codeless.ai.tools;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FileToolUtil - 文件工具类
 * 提供文件操作相关的公共工具方法
 *
 * @author ding
 * @date 2025/12/13
 */
@Slf4j
public class FileToolUtil {
    
    /**
     * 解析并验证安全路径
     * 验证相对路径本身的安全性，不进行项目路径拼接
     *
     * @param relativePath 相对路径
     * @return 验证后的相对路径字符串，如果路径不安全则返回null
     */
    public static String resolveSafePath(String relativePath) {
        try {
            Path path = Paths.get(relativePath);
            
            // 如果是绝对路径，拒绝操作（安全考虑）
            if (path.isAbsolute()) {
                log.warn("拒绝绝对路径操作: {}", relativePath);
                return null;
            }
            
            // 检查路径中是否包含危险的路径遍历（如 ../）
            String normalizedPath = path.normalize().toString();
            if (normalizedPath.contains("..") || normalizedPath.startsWith("/")) {
                log.warn("拒绝包含路径遍历的路径: {}", relativePath);
                return null;
            }
            
            return normalizedPath;
        } catch (Exception e) {
            log.error("路径解析失败: {}", relativePath, e);
            return null;
        }
    }
}

