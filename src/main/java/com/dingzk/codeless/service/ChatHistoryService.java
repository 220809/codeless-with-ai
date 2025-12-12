package com.dingzk.codeless.service;

import com.dingzk.codeless.model.dto.chathistory.ChatHistorySearchRequest;
import com.dingzk.codeless.model.entity.ChatHistory;
import com.dingzk.codeless.model.entity.User;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.ChatMemory;

import java.time.LocalDateTime;

/**
 * 聊天历史服务层
 *
 * @author dingzk
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 加载聊天历史到 ChatMemory
     * @param appId 应用id
     * @param chatMemory chatMemory
     * @param fetchSize 加载条数
     * @return 加载条数
     */
    int loadChatHistoryToChatMemory(long appId, ChatMemory chatMemory, int fetchSize);

    /**
     * 新增对话消息
     * @param appId 应用id
     * @param messageContent 消息内容
     * @param messageType 消息类型
     * @param loginUser 登录用户
     * @return 新增结果
     */
    long addChatHistory(Long appId, String messageContent, String messageType, User loginUser);

    /**
     * 根据应用id删除聊天历史（级联删除）
     *
     * @param appId 应用id
     * @return 删除结果
     */
    boolean deleteChatHistoryByAppId(Long appId);

    /**
     * 分页查询聊天历史
     * @param appId 应用id
     * @param pageSize 页大小
     * @param lastRecentCreateTime 最近最新一条消息的创建时间（用于分页查询更早的消息）
     * @param loginUser 登录用户
     * @return 分页结果
     */
    Page<ChatHistory> pageListChatHistory(Long appId, Integer pageSize, LocalDateTime lastRecentCreateTime, User loginUser);

    /**
     * 管理员分页查询聊天历史
     *
     * @param chatHistorySearchRequest 搜索请求
     * @return 分页结果
     */
    Page<ChatHistory> adminPageListChatHistory(ChatHistorySearchRequest chatHistorySearchRequest);
}
