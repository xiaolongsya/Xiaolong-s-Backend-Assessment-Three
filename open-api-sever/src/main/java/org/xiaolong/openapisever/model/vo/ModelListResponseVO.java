package org.xiaolong.openapisever.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
//模型列表响应
public class ModelListResponseVO
{
    @Schema(description = "对象")
    private String object = "list";
    @Schema(description = "数据")
    private List<ModelVO> data;


    @Data
    public static class ModelVO
    {
        @Schema(description = "模型id")
        private String id;
        @Schema(description = "对象")
        private String object = "model";
        @Schema(description = "创建时间")
        private Long created;
        @Schema(description = "是否可用")
        private String ownedBy;
    }
}
