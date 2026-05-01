package com.furkan.ecommerce.auth.internal.exception;

import com.furkan.ecommerce.common.exception.BaseException;

public class EmailAlreadyExistsException extends BaseException {
    public EmailAlreadyExistsException(String email) {
        super("EMAIL_ALREADY_EXISTS", "Email already exists: " + email);
    }
}
