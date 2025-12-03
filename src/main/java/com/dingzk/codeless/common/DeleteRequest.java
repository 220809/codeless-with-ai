package com.dingzk.codeless.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 删除请求
 *
 * @author ding
 * @date 2025/12/3 20:02
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * 删除id
     */
    private Long id;

    @Serial
    private static final long serialVersionUID = 1L;
}
