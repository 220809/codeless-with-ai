package com.dingzk.codeless.model.dto.app;

import com.mybatisflex.annotation.Column;
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
public class AppAdminUpdateRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用封面
     */
    @Column("cover")
    private String cover;

    /**
     * 优先级
     */
    @Column("priority")
    private Integer priority;

    @Serial
    private static final long serialVersionUID = 1L;
}
