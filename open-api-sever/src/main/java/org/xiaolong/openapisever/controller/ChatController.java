package org.xiaolong.openapisever.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.xiaolong.openapisever.entity.ChatCompletion;
import org.xiaolong.openapisever.model.dto.ChatCompletionRequestDTO;
import org.xiaolong.openapisever.model.vo.ChatCompletionResponseVO;
import org.xiaolong.openapisever.service.ChatService;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestController
@RequestMapping("/v1/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private WebClient webClient;

    @Value("${deepseekApi.url")
    private String deepseekUrl;

    @Value("${deepseekApi.key")
    private String deepseekKey;

    // --- 省略 getCompletionDetail, deleteCompletion, listModels 等已实现方法 ---

    @PostMapping(value = "/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object chatCompletions(@RequestBody ChatCompletionRequestDTO request, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) userId = "test_user_id";

        // 1. 预先持久化请求记录
        String completionId = chatService.saveRequest(userId, request);

        if (Boolean.TRUE.equals(request.getStream())) {
            return handleDeepSeekStream(request, completionId);
        } else {
            return handleDeepSeekBlocking(request, completionId);
        }
    }

    /**
     * 实战：调用 DeepSeek 流式接口
     */
    private Flux<String> handleDeepSeekStream(ChatCompletionRequestDTO request, String completionId) {
        StringBuilder fullContent = new StringBuilder();

        return webClient.post()
                .uri(deepseekUrl + "/chat/completions")
                .header("Authorization", "Bearer " + deepseekKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request) // 直接透传请求 DTO
                .retrieve()
                .bodyToFlux(String.class)
                .map(data -> {
                    // 2. 这里的 data 是 DeepSeek 返回的一帧数据
                    if (data.contains("\"content\":\"")) {
                        String content = parseContentFromJson(data);
                        fullContent.append(content);
                    }
                    // 3. 原样转发 SSE 帧
                    return data + "\n\n";
                })
                .doFinally(signalType -> {
                    // 4. 流结束时（正常或异常），将全量内容异步存入数据库
                    log.info("流输出结束，正在持久化完整内容...");
                    chatService.completeRequest(completionId, fullContent.toString());
                });
    }

    /**
     * 实战：调用 DeepSeek 非流式接口
     */
    private ChatCompletionResponseVO handleDeepSeekBlocking(ChatCompletionRequestDTO request, String completionId) {
        // 使用 WebClient 同步等待结果 (block)
        ChatCompletionResponseVO response = webClient.post()
                .uri(deepseekUrl + "/chat/completions")
                .header("Authorization", "Bearer " + deepseekKey)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponseVO.class)
                .block();

        if (response != null && !response.getChoices().isEmpty()) {
            String content = response.getChoices().get(0).getMessage().getContent();
            // 5. 更新数据库为完成状态
            chatService.completeRequest(completionId, content);
        }
        return response;
    }

    /**
     * 简单的正则工具：从 JSON 字符串中提取 content 字段
     */
    private String parseContentFromJson(String json) {
        Pattern pattern = Pattern.compile("\"content\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"");
        }
        return "";
    }
}