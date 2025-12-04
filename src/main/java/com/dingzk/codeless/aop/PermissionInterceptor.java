package com.dingzk.codeless.aop;

import com.dingzk.codeless.annotation.CheckPermission;
import com.dingzk.codeless.exception.ErrorCode;
import com.dingzk.codeless.model.enums.UserRoleEnum;
import com.dingzk.codeless.exception.BusinessException;
import com.dingzk.codeless.model.entity.User;
import com.dingzk.codeless.service.UserService;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 权限拦截器
 *
 * @author ding
 * @date 2025/12/4 16:35
 */
@Aspect
@Component
public class PermissionInterceptor {

    @Resource
    private UserService userService;

    @Around("@annotation(checkPermission)")
    public Object doIntercept(ProceedingJoinPoint joinPoint, CheckPermission checkPermission) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        User loginUser = userService.getLoginUser(request);

        if (loginUser.getUserRole() == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        UserRoleEnum userRoleEnum = UserRoleEnum.fromValue(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        String allowRole = checkPermission.allow();
        if (StrUtil.isBlank(allowRole)) {
            // 无权限要求，放行
            return joinPoint.proceed();
        }
        if (!allowRole.equals(userRoleEnum.getName())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return joinPoint.proceed();
    }
}