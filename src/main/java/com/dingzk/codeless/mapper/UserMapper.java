package com.dingzk.codeless.mapper;

import com.dingzk.codeless.common.SearchRequest;
import com.dingzk.codeless.model.dto.user.UserBaseDto;
import com.mybatisflex.core.BaseMapper;
import com.dingzk.codeless.model.entity.User;
import com.mybatisflex.core.query.QueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

/**
 * UserMapper
 *
 * @author ding
 * @date 2025/12/4 15:25
 */
public interface UserMapper extends BaseMapper<User> {
    default QueryWrapper buildQueryWrapper(UserBaseDto userBaseDto) {
        User user = new User();
        BeanUtils.copyProperties(userBaseDto, user);
        Long id = user.getId();
        String username = user.getUsername();
        String userAccount = user.getUserAccount();
        String password = user.getPassword();
        Integer gender = user.getGender();
        Integer userRole = user.getUserRole();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id, id != null)
                .like("username", username, StringUtils.isNotBlank(username))
                .eq("user_account", userAccount, StringUtils.isNotBlank(userAccount))
                .eq("password", password, StringUtils.isNotBlank(password))
                .eq("gender", gender, gender != null)
                .eq("user_role", userRole, userRole != null);
        if (userBaseDto instanceof SearchRequest searchRequest) {
            queryWrapper.orderBy(searchRequest.getSortField(),
                    SearchRequest.SortOrder.ASC.getOrder().equalsIgnoreCase(searchRequest.getSortOrder()));
        }
        return queryWrapper;
    }
}
