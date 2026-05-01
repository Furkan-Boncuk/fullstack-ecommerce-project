package com.furkan.ecommerce.common.exception;

public class BusinessException extends BaseException {
    public BusinessException(String code, String message) {
        super(code, message);
    }
}
