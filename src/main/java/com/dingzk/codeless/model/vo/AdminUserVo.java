package com.dingzk.codeless.model.vo;

import com.dingzk.codeless.model.entity.User;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class AdminUserVo implements Serializable {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户简介
     */
    private String userIntro;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别 0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 用户角色：0-user / 1-admin
     */
    private Integer userRole;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @Serial
    private static final long serialVersionUID = 1L;

    public static AdminUserVo fromUser(User user) {
        AdminUserVo adminUserVo = new AdminUserVo();
        BeanUtils.copyProperties(user, adminUserVo);
        return adminUserVo;
    }
}
