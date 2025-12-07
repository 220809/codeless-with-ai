package com.dingzk.codeless.model.dto.app;

import com.dingzk.codeless.common.SearchRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用搜索请求参数包装类
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppSearchRequest extends SearchRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用初始化的 prompt
     */
    private String initialPrompt;

    /**
     * 代码生成类型（枚举）
     */
    private String genFileType;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 用户 id
     */
    private Long userId;

    @Serial
    private static final long serialVersionUID = 1L;
}
