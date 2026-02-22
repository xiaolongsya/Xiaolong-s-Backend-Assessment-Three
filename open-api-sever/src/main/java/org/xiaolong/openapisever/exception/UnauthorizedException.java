package org.xiaolong.openapisever.exception;

public class UnauthorizedException extends RuntimeException
{
    public UnauthorizedException(String message) {
        super(message);
    }
}