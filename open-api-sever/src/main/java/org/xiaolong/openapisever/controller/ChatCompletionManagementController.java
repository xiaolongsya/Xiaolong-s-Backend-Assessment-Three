package org.xiaolong.openapisever.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xiaolong.openapisever.exception.NotFoundException;
import org.xiaolong.openapisever.model.vo.ChatCompletionCancelResponseVO;
import org.xiaolong.openapisever.model.vo.ChatCompletionDeleteResponseVO;
import org.xiaolong.openapisever.model.vo.ChatCompletionResponseVO;
import org.xiaolong.openapisever.service.ChatService;

@RestController
@RequestMapping("/v1/chat/completions")
public class ChatCompletionManagementController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/{completionId}")
    public ChatCompletionResponseVO getCompletion(@PathVariable String completionId) {
        ChatCompletionResponseVO res = chatService.getCompletion(completionId);
        if (res == null) {
            throw new NotFoundException("Completion not found");
        }
        return res;
    }

    @DeleteMapping("/{completionId}")
    public ChatCompletionDeleteResponseVO deleteCompletion(@PathVariable String completionId) {
        ChatCompletionResponseVO existing = chatService.getCompletion(completionId);
        if (existing == null) {
            throw new NotFoundException("Completion not found");
        }
        return chatService.deleteCompletion(completionId);
    }

    @PostMapping("/{completionId}/cancel")
    public ChatCompletionCancelResponseVO cancelCompletion(@PathVariable String completionId) {
        ChatCompletionResponseVO existing = chatService.getCompletion(completionId);
        if (existing == null) {
            throw new NotFoundException("Completion not found");
        }
        return chatService.cancelCompletion(completionId);
    }
}
