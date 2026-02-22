package org.xiaolong.openapisever.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xiaolong.openapisever.exception.UnauthorizedException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理鉴权失败异常 (401)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(UnauthorizedException e) {
        // 构建文档要求的响应结构
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", e.getMessage()); // 通常是 "Invalid API key" [cite: 226]
        errorDetails.put("type", "authentication_error"); // [cite: 227]

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorDetails); // [cite: 225, 228]

        // 返回 401 状态码和指定的 JSON 格式
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * (可选) 处理其他兜底的全局异常 (500)
     * 这能防止服务崩溃时向客户端暴露包含代码细节的堆栈信息
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception e) {
        Map<String, String> errorDetails = new HashMap<>();
        errorDetails.put("message", "Internal server error: " + e.getMessage());
        errorDetails.put("type", "server_error");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", errorDetails);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}