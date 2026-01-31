package org.xiaolong.openapisever.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("chat_completion")
public class ChatCompletion
{
    @TableId(type = IdType.AUTO)
    @Schema(description = "id")
    private Long id;
    @Schema(description = "请求标识")
    private String completionId;
    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "模型")
    private String model;
    @Schema(description = "对话消息，json格式")
    private String messages;
    @Schema(description = "温度")
    private BigDecimal temperature;
    @Schema(description = "流式, 1为是，0为否")
    private Integer stream;
    @Schema(description = "响应")
    private String response;
    @Schema(description = "状态， 0-处理中， 1-完成，-2 取消， 3-失败")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;


}
