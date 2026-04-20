package org.xiaolong.openapisever.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaolong.openapisever.entity.AiModel;
import org.xiaolong.openapisever.model.vo.ModelListResponseVO;
import org.xiaolong.openapisever.service.AiModelService;

import java.util.List;

@RestController
@RequestMapping("/v1")
public class ModelsController {

    @Autowired
    private AiModelService aiModelService;

    @GetMapping("/models")
    public ModelListResponseVO listModels() {
        List<AiModel> models = aiModelService.listEnabledModels();

        List<ModelListResponseVO.ModelVO> data = models.stream()
                .map(m -> ModelListResponseVO.ModelVO.builder()
                        .id(m.getModelId())
                        .created(m.getCreated())
                        .ownedBy(m.getOwnedBy())
                        .build())
                .toList();

        return ModelListResponseVO.builder().data(data).build();
    }
}
