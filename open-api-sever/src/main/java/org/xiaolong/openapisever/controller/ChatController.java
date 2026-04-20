package org.xiaolong.openapisever.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.xiaolong.openapisever.model.dto.ChatCompletionRequestDTO;
import org.xiaolong.openapisever.service.ChatService;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/v1/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping(value = "/completions", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_EVENT_STREAM_VALUE})
    public Object chatCompletions(@RequestBody ChatCompletionRequestDTO request, HttpServletRequest httpRequest) {
        String userId = (String) httpRequest.getAttribute("userId");
        if (userId == null) userId = "test_user_id";

        if (Boolean.TRUE.equals(request.getStream())) {
            Flux<String> stream = chatService.createCompletionStream(userId, request);
            return stream;
        }

        return chatService.createCompletionBlocking(userId, request);
    }
}