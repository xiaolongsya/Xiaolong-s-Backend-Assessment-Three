package org.xiaolong.openapisever.service;

import org.xiaolong.openapisever.entity.ChatCompletion;
import org.xiaolong.openapisever.model.dto.ChatCompletionRequestDTO;

public interface ChatService {
    // 创建初始记录，返回生成的唯一 ID
    String saveRequest(String userId, ChatCompletionRequestDTO request);

    // 生成完成后，更新回复内容和状态
    void completeRequest(String id, String content);

    ChatCompletion getById(String completionId);

    boolean removeById(String id);
}
