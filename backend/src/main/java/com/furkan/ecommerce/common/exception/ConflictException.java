package com.furkan.ecommerce.common.exception;

public class ConflictException extends BaseException {
    public ConflictException(String code, String message) {
        super(code, message);
    }
}
