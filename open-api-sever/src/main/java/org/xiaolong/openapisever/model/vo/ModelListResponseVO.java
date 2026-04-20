package org.xiaolong.openapisever.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelListResponseVO {
    @Builder.Default
    private String object = "list";

    private List<ModelVO> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModelVO {
        private String id;

        @Builder.Default
        private String object = "model";

        private Long created;

        @JsonProperty("owned_by")
        private String ownedBy;
    }
}
