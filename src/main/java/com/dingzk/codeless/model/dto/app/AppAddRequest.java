package com.dingzk.codeless.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用创建请求参数包装类
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
@Data
public class AppAddRequest implements Serializable {
    /**
     * 应用初始化的 prompt
     */
    private String initialPrompt;

    @Serial
    private static final long serialVersionUID = 1L;
}
