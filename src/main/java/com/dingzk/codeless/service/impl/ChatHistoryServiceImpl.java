package com.dingzk.codeless.service.impl;

import com.dingzk.codeless.common.SearchRequest;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.mapper.ChatHistoryMapper;
import com.dingzk.codeless.model.dto.chathistory.ChatHistorySearchRequest;
import com.dingzk.codeless.model.entity.ChatHistory;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.model.enums.ChatMessageTypeEnum;
import com.dingzk.codeless.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 聊天历史服务实现类
 *
 * @author dingzk
 */
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    /**
     * 消息内容最大长度
     */
    private static final int MESSAGE_CONTENT_MAX_LENGTH = 65535;

    /**
     * 用户查询最大页面大小
     */
    private static final int USER_MAX_PAGE_SIZE = 20;

    @Override
    public long addChatHistory(Long appId, String messageContent, String messageType, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "非法的应用ID");
        ThrowUtils.throwIf(StringUtils.isBlank(messageContent), ErrorCode.BAD_PARAM_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(messageContent.length() > MESSAGE_CONTENT_MAX_LENGTH,
                ErrorCode.BAD_PARAM_ERROR, "消息内容长度过长");
        ChatMessageTypeEnum messageTypeEnum = ChatMessageTypeEnum.getByType(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.BAD_PARAM_ERROR, "不支持的消息类型");

        // 3. 构造聊天历史实体
        ChatHistory chatHistory = new ChatHistory();
        chatHistory.setAppId(appId);
        chatHistory.setMessageContent(messageContent);
        chatHistory.setMessageType(messageType);
        chatHistory.setUserId(loginUser.getId());

        // 4. 保存记录
        boolean result = this.save(chatHistory);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "保存聊天历史失败");

        return chatHistory.getId();
    }

    @Override
    public boolean deleteChatHistoryByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "非法的应用ID");

        QueryWrapper queryWrapper = QueryWrapper.create()
                .where("app_id", appId);

        return this.remove(queryWrapper);
    }

    @Override
    public Page<ChatHistory> pageListChatHistory(Long appId, Integer pageSize, LocalDateTime lastRecentCreateTime, User loginUser) {
        // 需要用户先登录
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "应用ID不合法");

        // 不做 app 权限校验，允许非创建者查看对话历史
        // 限制页面大小
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        } else if (pageSize > USER_MAX_PAGE_SIZE) {
            pageSize = USER_MAX_PAGE_SIZE;
        }
        ChatHistorySearchRequest searchRequest = new ChatHistorySearchRequest();
        searchRequest.setAppId(appId);
        searchRequest.setPageSize(pageSize);
        searchRequest.setLastRecentCreateTime(lastRecentCreateTime);
        // 默认创建时间倒序排列
        searchRequest.setSortField("create_time");
        searchRequest.setSortOrder(SearchRequest.SortOrder.DESC.getOrder());

        return doPageListChatHistory(searchRequest);
    }

    @Override
    public Page<ChatHistory> adminPageListChatHistory(ChatHistorySearchRequest chatHistorySearchRequest) {
        ThrowUtils.throwIf(chatHistorySearchRequest == null, ErrorCode.BAD_PARAM_ERROR, "请求参数不能为空");

        Long appId = chatHistorySearchRequest.getAppId();
        if (appId != null && appId <= 0) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR, "应用ID不合法");
        }

        return doPageListChatHistory(chatHistorySearchRequest);
    }

    /**
     * 执行分页查询聊天历史
     *
     * @param chatHistorySearchRequest 搜索请求
     * @return 分页结果
     */
    private Page<ChatHistory> doPageListChatHistory(ChatHistorySearchRequest chatHistorySearchRequest) {
        Long appId = chatHistorySearchRequest.getAppId();
        String messageType = chatHistorySearchRequest.getMessageType();
        Integer pageSize = chatHistorySearchRequest.getPageSize();

        // 构造查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("app_id", appId)
                .eq("deleted", 0);

        // 消息类型过滤
        if (messageType != null) {
            ChatMessageTypeEnum messageTypeEnum = ChatMessageTypeEnum.getByType(messageType);
            ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.BAD_PARAM_ERROR, "不支持的消息类型");
            queryWrapper.eq("message_type", messageType);
        }

        // 根据用户查询
        Long userId = chatHistorySearchRequest.getUserId();
        if (userId != null && userId > 0) {
            queryWrapper.eq("user_id", userId);
        }

        // 时间范围过滤（查询更早的消息）
        if (chatHistorySearchRequest.getLastRecentCreateTime() != null) {
            queryWrapper.lt("create_time", chatHistorySearchRequest.getLastRecentCreateTime());
        }

        // 默认创建时间倒序排列
        String sortField = chatHistorySearchRequest.getSortField();
        String sortOrder = chatHistorySearchRequest.getSortOrder();
        if (StringUtils.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, SearchRequest.SortOrder.ASC.getOrder().equalsIgnoreCase(sortOrder));
        } else {
            queryWrapper.orderBy("create_time", false);
        }

        // 执行分页查询
        Page<ChatHistory> page = this.page(Page.of(1, pageSize), queryWrapper);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new Page<>(Collections.emptyList(), 1, pageSize, 0);
        }

        return page;
    }
}
