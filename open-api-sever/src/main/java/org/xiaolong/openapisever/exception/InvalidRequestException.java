package org.xiaolong.openapisever.exception;

/**
 * 对齐 OpenAI 风格的 400 invalid_request_error
 */
public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) {
        super(message);
    }
}
