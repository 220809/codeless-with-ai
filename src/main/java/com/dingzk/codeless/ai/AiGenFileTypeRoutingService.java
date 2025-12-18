package com.dingzk.codeless.ai;

import com.dingzk.codeless.model.enums.GenFileTypeEnum;
import dev.langchain4j.service.SystemMessage;

/**
 * 文件类型路由服务
 *
 * @author ding
 * @date 2025/12/18 16:32
 */
public interface AiGenFileTypeRoutingService {

    /**
     * 根据初始提示判断生成文件类型
     * @param initialPrompt 初始提示
     * @return 生成文件类型
     */
    @SystemMessage(fromResource = "prompts/routing_file_type_prompt.txt")
    GenFileTypeEnum routeGenFileType(String initialPrompt);
}
