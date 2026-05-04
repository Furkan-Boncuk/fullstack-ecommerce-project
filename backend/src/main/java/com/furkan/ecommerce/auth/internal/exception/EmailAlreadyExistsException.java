package com.furkan.ecommerce.auth.internal.exception;

import com.furkan.ecommerce.common.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("EMAIL_ALREADY_EXISTS", "Email already exists: " + email);
    }
}
