package org.xiaolong.openapisever.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.xiaolong.openapisever.model.dto.MessageDTO;

import java.util.List;

/**
 * 对应 OpenAI 非流式响应结构
 */
@Data
@Builder
@NoArgsConstructor  // 关键1：添加无参构造函数（Jackson反序列化必需）
@AllArgsConstructor // 关键2：配合@Builder生成全参构造函数
public class ChatCompletionResponseVO {
    // 此次生成的唯一 ID (需调用你 utils 里的 ID 生成器) [cite: 51, 169]
    private String id;

    // 对象类型，固定为 "chat.completion" [cite: 170]
    @Builder.Default
    private String object = "chat.completion";

    // 创建时的秒级时间戳 [cite: 171]
    private Long created;

    // 使用的模型名称 [cite: 172]
    private String model;

    // 生成的选择列表（OpenAI 支持一次生成多个结果，我们默认回一个） [cite: 173]
    private List<ChoiceVO> choices;

    @Data
    @Builder
    @NoArgsConstructor  // 内部类同样需要添加无参构造函数
    @AllArgsConstructor
    public static class ChoiceVO {
        // 结果索引 [cite: 175]
        private Integer index;

        // 消息内容 [cite: 176]
        private MessageDTO message;

        // 停止原因，正常结束通常为 "stop"
        // 关键3：添加JsonProperty映射下划线字段（如果响应里是finish_reason）
        @JsonProperty("finish_reason")
        private String finish_reason;
    }
}