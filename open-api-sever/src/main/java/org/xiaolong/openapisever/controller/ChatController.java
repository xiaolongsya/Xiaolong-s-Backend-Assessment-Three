package org.xiaolong.openapisever.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.xiaolong.openapisever.model.dto.ChatCompletionRequestDTO;
import org.xiaolong.openapisever.model.vo.ChatCompletionResponseVO;
import org.xiaolong.openapisever.service.ChatService;
import reactor.core.publisher.Flux;

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

    // 阿里云百炼配置（对应yml中的dashscope）
    @Value("${dashscope.key}")
    private String dashscopeKey;

    @Value("${dashscope.url}")
    private String dashscopeUrl;

    @PostMapping(value = "/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object chatCompletions(@RequestBody ChatCompletionRequestDTO request, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) userId = "test_user_id";

        // 1. 预先持久化请求记录
        String completionId = chatService.saveRequest(userId, request);

        // 打印配置验证（测试用，生产环境建议删除）
        log.info("=== 阿里云百炼配置验证 ===");
        log.info("URL: {}", dashscopeUrl);
        log.info("Key: {}", dashscopeKey == null ? "NULL" : "******" + dashscopeKey.substring(dashscopeKey.length() - 6));
        log.info("请求模型: {}", request.getModel());
        log.info("是否流式: {}", request.getStream());

        try {
            if (Boolean.TRUE.equals(request.getStream())) {
                return handleDashScopeStream(request, completionId);
            } else {
                return handleDashScopeBlocking(request, completionId);
            }
        } catch (Exception e) {
            log.error("调用阿里云百炼接口异常", e);
            // 异常时更新数据库状态
            chatService.completeRequest(completionId, "接口调用异常：" + e.getMessage());
            throw e;
        }
    }

    /**
     * 调用阿里云百炼 流式接口（兼容OpenAI格式）
     */
    private Flux<String> handleDashScopeStream(ChatCompletionRequestDTO request, String completionId) {
        StringBuilder fullContent = new StringBuilder();

        return webClient.post()
                // 拼接完整接口地址：/compatible-mode/v1/chat/completions
                .uri(dashscopeUrl)
                // 关键修复：改回Bearer认证格式（百炼兼容接口要求）
                .header("Authorization", "Bearer " + dashscopeKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToFlux(String.class)
                .map(data -> {
                    // 过滤空数据和结束标记
                    if (data == null || data.isEmpty() || data.contains("[DONE]")) {
                        return data;
                    }
                    // 提取并拼接流式内容
                    if (data.contains("\"content\":\"")) {
                        String content = parseContentFromJson(data);
                        fullContent.append(content);
                        log.debug("流式内容片段: {}", content);
                    }
                    // 原样返回SSE帧（保持和前端兼容）
                    return data + "\n\n";
                })
                .doFinally(signalType -> {
                    // 流结束时持久化完整内容
                    log.info("流式请求结束，完整回复内容: {}", fullContent);
                    chatService.completeRequest(completionId, fullContent.toString());
                })
                .onErrorResume(e -> {
                    log.error("流式接口调用异常", e);
                    chatService.completeRequest(completionId, "流式调用异常：" + e.getMessage());
                    return Flux.just("data: {\"error\":\"" + e.getMessage() + "\"}\n\n");
                });
    }

    /**
     * 调用阿里云百炼 非流式接口
     */
    private ChatCompletionResponseVO handleDashScopeBlocking(ChatCompletionRequestDTO request, String completionId) {
        // 同步调用百炼接口
        ChatCompletionResponseVO response = webClient.post()
                .uri(dashscopeUrl)
                // 关键修复：改回Bearer认证格式
                .header("Authorization", "Bearer " + dashscopeKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatCompletionResponseVO.class)
                .block();

        // 处理返回结果
        if (response == null) {
            log.warn("百炼非流式接口返回空响应");
            chatService.completeRequest(completionId, "接口返回空响应");
            return null;
        }

        if (!response.getChoices().isEmpty()) {
            String content = response.getChoices().get(0).getMessage().getContent();
            log.info("非流式回复内容: {}", content);
            chatService.completeRequest(completionId, content);
        } else {
            log.warn("百炼接口返回无choices数据");
            chatService.completeRequest(completionId, "接口返回无有效内容");
        }

        return response;
    }

    /**
     * 提取JSON中的content字段（兼容百炼返回格式）
     */
    private String parseContentFromJson(String json) {
        try {
            Pattern pattern = Pattern.compile("\"content\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(json);
            if (matcher.find()) {
                // 还原转义字符
                return matcher.group(1)
                        .replace("\\n", "\n")
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\");
            }
        } catch (Exception e) {
            log.error("解析content字段异常", e);
        }
        return "";
    }
}