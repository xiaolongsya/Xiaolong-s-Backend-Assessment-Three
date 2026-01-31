package org.xiaolong.openapisever.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

//对话消息请求,核心请求
public class ChatCompletionRequestDTO
{
    @Schema(description = "模型")
    private String model;
    @Schema(description = "对话消息")
    private List<MessageDTO> messages;
    @Schema(description = "是否流式返回")
    private Boolean stream = false;
    @Schema(description = "温度")
    private Double temperature;

}
