package org.xiaolong.openapisever.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xiaolong.openapisever.entity.AiModel;
import org.xiaolong.openapisever.exception.InvalidRequestException;
import org.xiaolong.openapisever.mapper.AiModelMapper;
import org.xiaolong.openapisever.service.AiModelService;

import java.util.List;

@Service
public class AiModelServiceImpl implements AiModelService {

    @Autowired
    private AiModelMapper aiModelMapper;

    @Override
    public void assertModelAvailable(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            throw new InvalidRequestException("Model is required");
        }

        LambdaQueryWrapper<AiModel> query = new LambdaQueryWrapper<AiModel>()
                .eq(AiModel::getModelId, modelId)
                .eq(AiModel::getEnabled, 1);

        AiModel model = aiModelMapper.selectOne(query);
        if (model == null) {
            throw new InvalidRequestException("Model not available");
        }
    }

    @Override
    public List<AiModel> listEnabledModels() {
        LambdaQueryWrapper<AiModel> query = new LambdaQueryWrapper<AiModel>()
                .eq(AiModel::getEnabled, 1);
        return aiModelMapper.selectList(query);
    }
}
