package com.furkan.ecommerce.auth.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Email(message = "{validation.email}") String email,
        @NotBlank(message = "{validation.required}") String password
) {}
