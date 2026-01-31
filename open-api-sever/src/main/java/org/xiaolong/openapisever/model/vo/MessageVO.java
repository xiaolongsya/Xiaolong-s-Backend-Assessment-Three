package org.xiaolong.openapisever.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
//相应消息
public class MessageVO
{
    @Schema(description = "角色")
    private String role = "assistant";
    @Schema(description = "内容")
    private String content;
}
