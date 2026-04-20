package org.xiaolong.openapisever.service;

import org.xiaolong.openapisever.entity.ChatCompletion;
import org.xiaolong.openapisever.model.dto.ChatCompletionRequestDTO;
import org.xiaolong.openapisever.model.vo.ChatCompletionResponseVO;
import org.xiaolong.openapisever.model.vo.ChatCompletionCancelResponseVO;
import org.xiaolong.openapisever.model.vo.ChatCompletionDeleteResponseVO;
import reactor.core.publisher.Flux;

public interface ChatService {
    // 创建初始记录，返回生成的唯一 ID
    String saveRequest(String userId, ChatCompletionRequestDTO request);

    // 生成完成后，更新回复内容和状态
    void completeRequest(String id, String content);

    ChatCompletion getById(String completionId);

    boolean removeById(String id);

    /**
     * 非流式：校验模型白名单 -> 持久化请求 -> 调用上游 -> 持久化结果
     */
    ChatCompletionResponseVO createCompletionBlocking(String userId, ChatCompletionRequestDTO request);

    /**
     * 流式：校验模型白名单 -> 持久化请求 -> 调用上游(SSE) -> 转发 chunk -> 流结束后持久化结果
     */
    Flux<String> createCompletionStream(String userId, ChatCompletionRequestDTO request);

    /**
     * 获取某次生成结果（OpenAI 风格响应）
     */
    ChatCompletionResponseVO getCompletion(String completionId);

    /**
     * 删除某次生成结果
     */
    ChatCompletionDeleteResponseVO deleteCompletion(String completionId);

    /**
     * 取消正在进行的生成
     */
    ChatCompletionCancelResponseVO cancelCompletion(String completionId);
}
