package com.dingzk.codeless.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 应用部署请求
 *
 * @author ding
 * @date 2025/12/8 16:10
 */
@Data
public class AppDeployRequest implements Serializable {

    /**
     * id
     */
    private Long appId;

    @Serial
    private static final long serialVersionUID = 1L;
}
