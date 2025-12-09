package com.dingzk.codeless.controller;

import com.dingzk.codeless.annotation.CheckPermission;
import com.dingzk.codeless.common.BaseResponse;
import com.dingzk.codeless.common.RespUtils;
import com.dingzk.codeless.constant.UserConstant;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.exception.ThrowUtils;
import com.dingzk.codeless.model.dto.chathistory.ChatHistorySearchRequest;
import com.dingzk.codeless.model.entity.ChatHistory;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.service.ChatHistoryService;
import com.dingzk.codeless.service.UserService;
import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 聊天历史控制器
 *
 * @author dingzk
 */
@RestController
@RequestMapping("/chatHistory")
@Tag(name = "chatHistory", description = "聊天历史模块相关接口")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页查询应用聊天历史
     * @param appId 应用id
     * @param pageSize 每页大小
     * @param lastRecentCreateTime 最近最新一条消息的创建时间（用于分页查询更早的消息）
     * @param request 请求
     * @return 分页结果
     */
    @GetMapping("/app/{appId}")
    @Operation(summary = "分页查询应用聊天历史")
    public BaseResponse<Page<ChatHistory>> pageListMyChatHistory(@PathVariable("appId") Long appId,
                                                                 @RequestParam(defaultValue = "10") Integer pageSize,
                                                                 @RequestParam(required = false) LocalDateTime lastRecentCreateTime,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.BAD_PARAM_ERROR, "非法的应用ID");
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> chatHistoryPage = chatHistoryService.pageListChatHistory(appId, pageSize, lastRecentCreateTime, loginUser);
        return RespUtils.success(chatHistoryPage);
    }

    // region 管理员功能

    /**
     * 管理员分页查询聊天历史
     *
     * @param chatHistorySearchRequest 搜索请求
     * @return 分页结果
     */
    @PostMapping("/admin/list/page")
    @Operation(summary = "管理员分页查询聊天历史")
    @CheckPermission(allow = UserConstant.ROLE_ADMIN)
    public BaseResponse<Page<ChatHistory>> adminPageListChatHistory(@RequestBody ChatHistorySearchRequest chatHistorySearchRequest) {
        if (chatHistorySearchRequest == null) {
            throw new BusinessException(ErrorCode.BAD_PARAM_ERROR);
        }
        Page<ChatHistory> chatHistoryPage = chatHistoryService.adminPageListChatHistory(chatHistorySearchRequest);
        return RespUtils.success(chatHistoryPage);
    }

    // endregion
}
