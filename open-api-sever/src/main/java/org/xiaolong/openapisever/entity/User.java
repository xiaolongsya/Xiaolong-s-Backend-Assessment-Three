package org.xiaolong.openapisever.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User
{
    @TableId(type = IdType.AUTO)
    @Schema(description = "用户id")
    private Long id;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "API Key")
    private String apiKey;
    @Schema(description = "最大并发数")
    private Integer maxConcurrent;
    @Schema(description = "每日调用限额")
    private Integer dailyLimit;
    @Schema(description = "用户状态")
    private Integer status;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

}
