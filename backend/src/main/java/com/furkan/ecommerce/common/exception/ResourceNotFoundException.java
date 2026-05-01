package com.furkan.ecommerce.common.exception;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String code, String message) {
        super(code, message);
    }
}
