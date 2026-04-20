package org.xiaolong.openapisever.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ai_models")
public class AiModel {
    /**
     * 模型 ID（白名单主键），对应 OpenAI 的 model 字段。
     */
    @TableId("model_id")
    private String modelId;

    @TableField("owned_by")
    private String ownedBy;

    /**
     * 1=可用，0=不可用
     */
    private Integer enabled;

    /**
     * 秒级时间戳
     */
    private Long created;
}
