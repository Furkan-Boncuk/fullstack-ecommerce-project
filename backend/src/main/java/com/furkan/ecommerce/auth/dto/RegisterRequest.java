package com.furkan.ecommerce.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @Email(message = "{validation.email}") String email,
        @NotBlank(message = "{validation.required}") String password
) {}
