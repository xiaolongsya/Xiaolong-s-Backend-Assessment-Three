package org.xiaolong.openapisever.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
//对话消息请求子对象
public class MessageDTO
{
    @Schema(description = "角色")
    private String role;
    @Schema(description = "内容")
    private String content;
}
