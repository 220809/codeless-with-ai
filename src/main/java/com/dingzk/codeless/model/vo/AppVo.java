package com.dingzk.codeless.model.vo;

import com.dingzk.codeless.model.entity.App;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用视图对象
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
@Data
public class AppVo implements Serializable {

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
    private String cover;

    /**
     * 应用初始化的 prompt
     */
    private String initialPrompt;

    /**
     * 代码生成类型（枚举）
     */
    private String genFileType;

    /**
     * 部署标识
     */
    private String deployKey;

    /**
     * 部署时间
     */
    private LocalDateTime deployedTime;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建用户id
     */
    private Long userId;

    /**
     * 创建用户
     */
    private LoginUserVo user;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 将实体对象转换为视图对象
     *
     * @param app 应用实体
     * @return 应用视图对象
     */
    public static AppVo fromApp(App app, LoginUserVo userVo) {
        AppVo appVo = new AppVo();
        BeanUtils.copyProperties(app, appVo);
        appVo.setUser(userVo);
        return appVo;
    }
}
