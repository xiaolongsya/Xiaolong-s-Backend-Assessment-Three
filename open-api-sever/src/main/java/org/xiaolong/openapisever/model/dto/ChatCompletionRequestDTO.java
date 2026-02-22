package org.xiaolong.openapisever.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 对应 OpenAI Chat Completion 请求结构
 */
@Data
public class ChatCompletionRequestDTO
 {
    // 必须：使用的模型 ID，如 "gpt-3.5-turbo" [cite: 46, 156]
    private String model;

    // 必须：之前的对话上下文列表 [cite: 47, 157]
    private List<MessageDTO> messages;

    // 可选：是否开启流式输出，默认为 false [cite: 48, 55, 166]
    private Boolean stream = false;

    // 可选：采样温度，控制生成随机性 [cite: 49, 165]
    private Double temperature = 1.0;
}