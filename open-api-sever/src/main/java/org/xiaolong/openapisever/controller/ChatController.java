package org.xiaolong.openapisever.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.xiaolong.openapisever.entity.ChatCompletion;
import org.xiaolong.openapisever.model.dto.ChatCompletionRequestDTO;
import org.xiaolong.openapisever.model.dto.MessageDTO;
import org.xiaolong.openapisever.model.vo.ChatCompletionResponseVO;
import org.xiaolong.openapisever.service.ChatService;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 对话补全控制器
 * 兼容 OpenAI API 规范
 */
@Slf4j
@RestController
@RequestMapping("/v1/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;


    /**
     * 获取指定生成记录详情
     * GET /v1/chat/completions/{id}
     */
    @GetMapping("/completions/{id}")
    public Object getCompletionDetail(@PathVariable String id) {
        // 直接使用 MyBatis Plus 的 service 查询
        ChatCompletion completion = chatService.getById(id);
        if (completion == null) {
            return Collections.singletonMap("error", "未找到该记录");
        }
        return completion;
    }

    /**
     * 删除生成记录
     * DELETE /v1/chat/completions/{id}
     */
    @DeleteMapping("/completions/{id}")
    public Object deleteCompletion(@PathVariable String id) {
        boolean removed = chatService.removeById(id);
        return Collections.singletonMap("deleted", removed);
    }


    /**
     * 获取某一次生成结果 (对应考核：生成结果管理)
     * GET /v1/chat/completions/{completionId}
     */
    @GetMapping("/completions/{completionId}")
    public Object getCompletion(@PathVariable String completionId) {
        // 从数据库查询该记录
        ChatCompletion record = chatService.getById(completionId);

        if (record == null) {
            return Collections.singletonMap("error",
                    Collections.singletonMap("message", "Record not found"));
        }
        return record;
    }

    /**
     * 列出所有可用的模型 (对应考核：模型管理)
     * GET /v1/models
     */
    @GetMapping("/models")
    public Object listModels() {
        // 返回符合 OpenAI 规范的模型列表格式
        Map<String, Object> model = new HashMap<>();
        model.put("id", "gpt-3.5-turbo");
        model.put("object", "model");
        model.put("owned_by", "system");

        return Collections.singletonMap("data", Collections.singletonList(model));
    }
    /**
     * 对话补全接口 (核心)
     * 必须支持 model, messages, stream 等参数 [cite: 45-49]
     */
    @PostMapping(value = "/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object chatCompletions(@RequestBody ChatCompletionRequestDTO request, HttpServletRequest httpRequest) {

        // 1. 从 HttpServletRequest 中获取用户 ID (这里解决了你截图中的报错)
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) userId = "test_user_id";

        // 2. 核心要求：每一生成请求必须生成唯一 id 并持久化到数据库 [cite: 50-52]
        String completionId = chatService.saveRequest(userId, request);

        // 3. 根据 stream 参数走不同的响应逻辑 [cite: 54-58]
        if (Boolean.TRUE.equals(request.getStream())) {
            return handleStreamResponse(request, completionId);
        } else {
            return handleBlockingResponse(request, completionId);
        }
    }

    /**
     * 处理非流式请求 (stream=false)
     */
    private ChatCompletionResponseVO handleBlockingResponse(ChatCompletionRequestDTO request, String completionId) {
        String mockContent = "这是一个非流式的模拟回复。";

        // 4. 更新数据库状态为已完成 [cite: 62]
        chatService.completeRequest(completionId, mockContent);

        return ChatCompletionResponseVO.builder()
                .id(completionId)
                .object("chat.completion")
                .created(System.currentTimeMillis() / 1000)
                .model(request.getModel())
                .choices(Collections.singletonList(
                        ChatCompletionResponseVO.ChoiceVO.builder()
                                .index(0)
                                .message(new MessageDTO("assistant", mockContent))
                                .finish_reason("stop")
                                .build()
                ))
                .build();
    }

    /**
     * 处理流式请求 (stream=true)
     * 每一行必须以 "data: " 开头 [cite: 191-193]
     */
    private Flux<String> handleStreamResponse(ChatCompletionRequestDTO request, String completionId) {
        String[] chunks = {"你好", "，我是", "流式", "输出", "。"};
        StringBuilder fullContent = new StringBuilder();

        return Flux.fromArray(chunks)
                .delayElements(Duration.ofMillis(300)) // 模拟延迟 [cite: 60]
                .map(content -> {
                    fullContent.append(content);
                    // 构造符合 OpenAI 流式规范的 JSON [cite: 191]
                    String jsonChunk = String.format(
                            "{\"id\":\"%s\",\"object\":\"chat.completion.chunk\",\"created\":%d,\"model\":\"%s\",\"choices\":[{\"index\":0,\"delta\":{\"content\":\"%s\"},\"finish_reason\":null}]}",
                            completionId, System.currentTimeMillis() / 1000, request.getModel(), content
                    );
                    return "data: " + jsonChunk + "\n\n";
                })
                .concatWith(Flux.defer(() -> {
                    // 5. 生成结束后异步更新数据库 [cite: 62]
                    chatService.completeRequest(completionId, fullContent.toString());
                    return Flux.just("data: [DONE]\n\n");
                }));
    }
}