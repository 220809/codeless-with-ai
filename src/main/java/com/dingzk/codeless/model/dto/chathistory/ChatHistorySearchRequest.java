package com.dingzk.codeless.model.dto.chathistory;

import com.dingzk.codeless.common.SearchRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天历史搜索请求参数包装类
 *
 * @author dingzk
 * @date 2025/12/9
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistorySearchRequest extends SearchRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 最近最新一条消息的创建时间（用于分页查询更早的消息）
     */
    private LocalDateTime lastRecentCreateTime;

    /**
     * 消息类型 user/ai
     */
    private String messageType;

    /**
     * 用户id
     */
    private Long userId;

    @Serial
    private static final long serialVersionUID = 1L;
}
