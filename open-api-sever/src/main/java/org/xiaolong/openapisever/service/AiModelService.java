package org.xiaolong.openapisever.service;

import org.xiaolong.openapisever.entity.AiModel;

import java.util.List;

public interface AiModelService {
    /**
     * 校验模型是否在白名单且 enabled=1，否则抛出 InvalidRequestException
     */
    void assertModelAvailable(String modelId);

    /**
     * 列出所有 enabled=1 的模型
     */
    List<AiModel> listEnabledModels();
}
