package org.xiaolong.openapisever.service.impl;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.xiaolong.openapisever.entity.ChatCompletion;
import org.xiaolong.openapisever.mapper.ChatCompletionMapper;
import org.xiaolong.openapisever.model.dto.ChatCompletionRequestDTO;
import org.xiaolong.openapisever.model.dto.MessageDTO;
import org.xiaolong.openapisever.model.vo.ChatCompletionCancelResponseVO;
import org.xiaolong.openapisever.model.vo.ChatCompletionDeleteResponseVO;
import org.xiaolong.openapisever.model.vo.ChatCompletionResponseVO;
import org.xiaolong.openapisever.service.ChatService;
import org.xiaolong.openapisever.service.AiModelService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对话生成服务实现类
 * 负责生成记录的完整生命周期管理 [cite: 21, 62]
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatCompletionMapper chatCompletionMapper;

    @Autowired
    private AiModelService aiModelService;

    @Autowired
    private WebClient webClient;

    // 阿里云百炼配置（对应yml中的dashscope）
    @Value("${dashscope.key}")
    private String dashscopeKey;

    @Value("${dashscope.url}")
    private String dashscopeUrl;

    /**
     * 运行中流式请求的取消信号
     */
    private final ConcurrentHashMap<String, Sinks.Empty<Void>> streamCancelSignals = new ConcurrentHashMap<>();

    //根据id移除记录
    public boolean removeById(String id)
    {
        return chatCompletionMapper.deleteById(id) > 0;
    }

    //根据id查询记录
    public ChatCompletion getById(String completionId)
    {
        return chatCompletionMapper.selectById(completionId);
    }

    @Override
    public ChatCompletionResponseVO createCompletionBlocking(String userId, ChatCompletionRequestDTO request) {
        aiModelService.assertModelAvailable(request.getModel());

        String completionId = saveRequest(userId, request);

        try {
            ChatCompletionResponseVO response = webClient.post()
                    .uri(dashscopeUrl)
                    .header("Authorization", "Bearer " + dashscopeKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(ChatCompletionResponseVO.class)
                    .block();

            if (response == null) {
                log.warn("百炼非流式接口返回空响应");
                completeRequest(completionId, "接口返回空响应");
                return null;
            }

            // 对外返回服务端生成的 completionId，便于后续 GET/DELETE/CANCEL
            response.setId(completionId);
            if (response.getCreated() == null) {
                response.setCreated(System.currentTimeMillis() / 1000);
            }
            if (response.getModel() == null) {
                response.setModel(request.getModel());
            }

            if (response.getChoices() != null && !response.getChoices().isEmpty() && response.getChoices().get(0) != null) {
                String content = response.getChoices().get(0).getMessage() == null ? null : response.getChoices().get(0).getMessage().getContent();
                if (content != null) {
                    log.info("非流式回复内容: {}", content);
                    completeRequest(completionId, content);
                } else {
                    completeRequest(completionId, "接口返回无有效内容");
                }
            } else {
                log.warn("百炼接口返回无choices数据");
                completeRequest(completionId, "接口返回无有效内容");
            }

            return response;
        } catch (Exception e) {
            log.error("调用阿里云百炼接口异常", e);
            completeRequest(completionId, "接口调用异常：" + e.getMessage());
            throw e;
        }
    }

    @Override
    public Flux<String> createCompletionStream(String userId, ChatCompletionRequestDTO request) {
        aiModelService.assertModelAvailable(request.getModel());

        String completionId = saveRequest(userId, request);
        StringBuilder fullContent = new StringBuilder();

        Sinks.Empty<Void> cancelSink = Sinks.empty();
        streamCancelSignals.put(completionId, cancelSink);

        try {
            return webClient.post()
                    .uri(dashscopeUrl)
                    .header("Authorization", "Bearer " + dashscopeKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToFlux(String.class)
                    .map(data -> {
                        if (data == null || data.isEmpty()) return data;

                        String rewritten = rewriteSseIdIfPossible(data, completionId);

                        if (rewritten != null && rewritten.contains("[DONE]")) {
                            return rewritten;
                        }

                        if (rewritten != null && rewritten.contains("\"content\":\"")) {
                            String content = parseContentFromJson(rewritten);
                            fullContent.append(content);
                        }

                        return (rewritten == null ? data : rewritten) + "\n\n";
                    })
                    .takeUntilOther(cancelSink.asMono())
                    .doFinally(signalType -> {
                        log.info("流式请求结束，完整回复内容: {}", fullContent);
                        streamCancelSignals.remove(completionId);

                        ChatCompletion existing = chatCompletionMapper.selectById(completionId);
                        if (existing != null && "canceled".equals(existing.getStatus())) {
                            return;
                        }
                        completeRequest(completionId, fullContent.toString());
                    })
                    .onErrorResume(e -> {
                        log.error("流式接口调用异常", e);
                        streamCancelSignals.remove(completionId);

                        ChatCompletion existing = chatCompletionMapper.selectById(completionId);
                        if (existing == null || !"canceled".equals(existing.getStatus())) {
                            completeRequest(completionId, "流式调用异常：" + e.getMessage());
                        }
                        return Flux.just("data: {\"error\":\"" + e.getMessage() + "\"}\n\n");
                    });
        } catch (Exception e) {
            log.error("流式调用初始化异常", e);
            streamCancelSignals.remove(completionId);
            completeRequest(completionId, "流式调用异常：" + e.getMessage());
            throw e;
        }
    }

    @Override
    public ChatCompletionResponseVO getCompletion(String completionId) {
        ChatCompletion entity = getById(completionId);
        if (entity == null) {
            return null;
        }

        MessageDTO message = new MessageDTO("assistant", entity.getResponseContent());
        ChatCompletionResponseVO.ChoiceVO choice = ChatCompletionResponseVO.ChoiceVO.builder()
                .index(0)
                .message(message)
                .finish_reason("completed".equals(entity.getStatus()) ? "stop" : entity.getStatus())
                .build();

        return ChatCompletionResponseVO.builder()
                .id(entity.getId())
                .created(entity.getCreatedAt())
                .model(entity.getModel())
                .choices(List.of(choice))
                .build();
    }

    @Override
    public ChatCompletionDeleteResponseVO deleteCompletion(String completionId) {
        boolean deleted = removeById(completionId);
        return ChatCompletionDeleteResponseVO.builder()
                .id(completionId)
                .deleted(deleted)
                .build();
    }

    @Override
    public ChatCompletionCancelResponseVO cancelCompletion(String completionId) {
        ChatCompletion existing = getById(completionId);
        if (existing != null) {
            ChatCompletion update = new ChatCompletion();
            update.setId(completionId);
            update.setStatus("canceled");
            chatCompletionMapper.updateById(update);
        }

        Sinks.Empty<Void> cancelSink = streamCancelSignals.remove(completionId);
        if (cancelSink != null) {
            cancelSink.tryEmitEmpty();
        }

        return ChatCompletionCancelResponseVO.builder()
                .id(completionId)
                .build();
    }

    private String rewriteSseIdIfPossible(String data, String completionId) {
        String trimmed = data.trim();
        if (!trimmed.startsWith("data:")) {
            return data;
        }

        String payload = trimmed.substring("data:".length()).trim();
        if (payload.isEmpty() || "[DONE]".equals(payload)) {
            return trimmed;
        }

        try {
            JSONObject json = JSON.parseObject(payload);
            json.put("id", completionId);
            return "data: " + json.toJSONString();
        } catch (Exception ignore) {
            return data;
        }
    }

    /**
     * 提取JSON中的content字段（兼容百炼返回格式）
     */
    private String parseContentFromJson(String json) {
        try {
            String payload = json;
            String trimmed = json.trim();
            if (trimmed.startsWith("data:")) {
                payload = trimmed.substring("data:".length()).trim();
            }

            JSONObject obj = JSON.parseObject(payload);
            JSONArray choices = obj.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject first = choices.getJSONObject(0);
                if (first != null) {
                    JSONObject delta = first.getJSONObject("delta");
                    if (delta != null) {
                        String content = delta.getString("content");
                        if (content != null) return content;
                    }
                    JSONObject message = first.getJSONObject("message");
                    if (message != null) {
                        String content = message.getString("content");
                        if (content != null) return content;
                    }
                }
            }
        } catch (Exception ignored) {
            // fallback to regex
            try {
                Pattern pattern = Pattern.compile("\\\"content\\\":\\\"(.*?)\\\"");
                Matcher matcher = pattern.matcher(json);
                if (matcher.find()) {
                    return matcher.group(1)
                            .replace("\\n", "\n")
                            .replace("\\\"", "\"")
                            .replace("\\\\", "\\");
                }
            } catch (Exception e) {
                log.error("解析content字段异常", e);
            }
        }
        return "";
    }
    /**
     * 持久化初始请求
     */
    @Override
    public String saveRequest(String userId, ChatCompletionRequestDTO request) {
        // 生成符合 OpenAI 风格的 ID [cite: 51, 169]
        String completionId = "chatcmpl-" + IdUtil.simpleUUID();

        ChatCompletion entity = new ChatCompletion();
        entity.setId(completionId);
        entity.setUserId(userId);
        entity.setModel(request.getModel());
        // 将请求的消息列表序列化为 JSON 存储 [cite: 157-163]
        entity.setRequestMessages(JSON.toJSONString(request.getMessages()));
        entity.setStatus("processing");
        entity.setCreatedAt(System.currentTimeMillis() / 1000);

        chatCompletionMapper.insert(entity);
        return completionId;
    }

    /**
     * 更新最终生成结果 [cite: 62]
     */
    @Override
    public void completeRequest(String id, String content) {
        ChatCompletion entity = new ChatCompletion();
        entity.setId(id);
        entity.setResponseContent(content);
        entity.setStatus("completed");

        chatCompletionMapper.updateById(entity);
    }
}