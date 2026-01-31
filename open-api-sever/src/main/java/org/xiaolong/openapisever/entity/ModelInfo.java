package org.xiaolong.openapisever.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("model_info")
public class ModelInfo
{
    @TableId(type = IdType.AUTO)
    @Schema(description = "id")
    private Long id;
    @Schema(description = "模型id")
    private String modelId;
    @Schema(description = "所有者")
    private Long ownedBy;
    @Schema(description = "是否可用")
    private Integer available;
    @Schema(description = "创建时间")
    private LocalDateTime createdTime;

}
