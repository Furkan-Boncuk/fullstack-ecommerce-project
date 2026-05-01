package com.furkan.ecommerce.common.exception;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }
}
