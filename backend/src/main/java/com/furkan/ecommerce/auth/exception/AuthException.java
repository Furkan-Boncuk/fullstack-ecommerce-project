package com.furkan.ecommerce.auth.exception;

import com.furkan.ecommerce.common.exception.UnauthorizedException;

public class AuthException extends UnauthorizedException {
    public AuthException(String message) {
        super(message);
    }
}
