package com.furkan.ecommerce.common.exception;

public class BaseException extends RuntimeException {
    private final String code;

    public BaseException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
