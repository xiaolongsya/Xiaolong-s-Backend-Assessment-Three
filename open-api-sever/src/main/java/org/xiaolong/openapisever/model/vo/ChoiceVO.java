package org.xiaolong.openapisever.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
//生成结果选项
public class ChoiceVO
{
    @Schema(description = "序号")
    private Integer index = 0;
    @Schema(description = "消息")
    private MessageVO message;
    @Schema(description = "完成原因")
    private String finishReason = "stop";
}
