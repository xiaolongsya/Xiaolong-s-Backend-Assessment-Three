package org.xiaolong.openapisever.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

//对话消息请求,核心请求
public class ChatCompletionRequestDTO
{
    @Schema(description = "模型")
    @NotBlank(message = "model参数不能为空")
    private String model;
    @NotEmpty(message = "messages参数不能为空")
    @Schema(description = "对话消息")
    private List<MessageDTO> messages;
    @Schema(description = "是否流式返回")
    private Boolean stream = false;
    @Schema(description = "温度")
    private Double temperature = 0.7;

}
