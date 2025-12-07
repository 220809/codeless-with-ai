package com.dingzk.codeless.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用更新请求参数包装类
 * 用户侧修改, 暂时仅支持修改应用名
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
@Data
public class AppUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String name;

    @Serial
    private static final long serialVersionUID = 1L;
}
