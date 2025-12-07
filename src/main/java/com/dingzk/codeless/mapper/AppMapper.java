package com.dingzk.codeless.mapper;

import com.dingzk.codeless.common.SearchRequest;
import com.dingzk.codeless.model.dto.app.AppSearchRequest;
import com.dingzk.codeless.model.entity.App;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.query.QueryWrapper;

/**
 * 应用映射层
 *
 * @author ding
 * @date 2025/12/7 14:45
 */
public interface AppMapper extends BaseMapper<App> {

    /**
     * 构建查询条件
     * @param appSearchRequest 应用搜索请求
     * @return 查询条件
     */
    default QueryWrapper buildQueryWrapper(AppSearchRequest appSearchRequest) {

        return QueryWrapper.create()
                .eq("id", appSearchRequest.getId(), appSearchRequest.getId() != null)
                .like("name", appSearchRequest.getName(), appSearchRequest.getName() != null)
                .like("initial_prompt", appSearchRequest.getInitialPrompt(), appSearchRequest.getInitialPrompt() != null)
                .eq("gen_file_Type", appSearchRequest.getGenFileType(), appSearchRequest.getGenFileType() != null)
                .eq("deploy_key", appSearchRequest.getDeployKey(), appSearchRequest.getDeployKey() != null)
                .eq("priority", appSearchRequest.getPriority(), appSearchRequest.getPriority() != null)
                .eq("user_id", appSearchRequest.getUserId(), appSearchRequest.getUserId() != null)
                .orderBy(appSearchRequest.getSortField(),
                        SearchRequest.SortOrder.ASC.getOrder().equalsIgnoreCase(appSearchRequest.getSortOrder()));
    }
}
