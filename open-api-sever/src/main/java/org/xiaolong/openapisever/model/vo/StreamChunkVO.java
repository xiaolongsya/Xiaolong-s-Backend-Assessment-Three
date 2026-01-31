package org.xiaolong.openapisever.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
// 流式响应
public class StreamChunkVO
{
    @Schema(description = "id")
    private String id;
    @Schema(description = "choices")
    private List<DeltaChoiceVO> choices;




    @Data
    public static class DeltaChoiceVO
    {
        @Schema(description = "序号")
        private Integer index = 0;
        @Schema(description = "消息")
        private DeltaVO delta;
    }

    @Data
    public static class DeltaVO
    {
        @Schema(description = "内容")
        private String content;
    }
}
