package org.xiaolong.openapisever.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

@Data
public class ChatCompletion {
    @TableId(type = IdType.INPUT) // 因为我们要手动生成符合 OpenAI 规范的 ID
    private String id;
    private String userId;        // 从 JWT 拦截器中解析出来的用户标识
    private String model;         // 请求使用的模型
    private String requestMessages; // 存储请求的 JSON 字符串
    private String responseContent; // 存储生成的回答内容
    private String status;        // 状态：processing, completed, canceled [cite: 65]
    private Long createdAt;       // 创建时间戳
}