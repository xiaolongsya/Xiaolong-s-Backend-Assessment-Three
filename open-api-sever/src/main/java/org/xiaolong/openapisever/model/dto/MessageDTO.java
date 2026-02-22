package org.xiaolong.openapisever.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对应 OpenAI 规范中的消息对象
 * 用于接收和返回对话内容
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO
{
    // 角色：system(系统提示), user(用户提问), assistant(AI回答) [cite: 160, 162, 178]
    private String role;
    // 消息的具体文本内容 [cite: 160, 162, 179]
    private String content;
}