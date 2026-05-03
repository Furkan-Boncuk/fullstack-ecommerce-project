package com.furkan.ecommerce.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePaymentProfileRequest(
        @NotBlank(message = "{validation.required}")
        @Size(max = 80, message = "{validation.size}")
        String firstName,
        @NotBlank(message = "{validation.required}")
        @Size(max = 80, message = "{validation.size}")
        String lastName,
        @NotBlank(message = "{validation.required}")
        @Pattern(regexp = "^\\+90\\d{10}$", message = "{validation.phone}")
        String phoneNumber,
        @NotBlank(message = "{validation.required}")
        @Pattern(regexp = "^\\d{11}$", message = "{validation.identityNumber}")
        String identityNumber,
        @NotBlank(message = "{validation.required}")
        @Pattern(regexp = "^\\S+(?:\\s+\\S+)+$", message = "{validation.address}")
        @Size(max = 255, message = "{validation.size}")
        String address,
        @NotBlank(message = "{validation.required}")
        @Size(max = 80, message = "{validation.size}")
        String city,
        @NotBlank(message = "{validation.required}")
        @Size(max = 80, message = "{validation.size}")
        String country,
        @NotBlank(message = "{validation.required}")
        @Pattern(regexp = "^\\d{5}$", message = "{validation.zipCode}")
        String zipCode
) {}
