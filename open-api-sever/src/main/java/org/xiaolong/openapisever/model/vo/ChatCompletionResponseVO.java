package org.xiaolong.openapisever.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
//非流式响应
public class ChatCompletionResponseVO
{
    @Schema(description = "请求标识")
    private String id;
    @Schema(description = "对象")
    private String object = "chat.completion";
    @Schema(description = "时间戳-秒")
    private Long created;
    @Schema(description = "模型")
    private String model;
    @Schema(description = "使用量")
    private List<ChoiceVO> choices;
}
