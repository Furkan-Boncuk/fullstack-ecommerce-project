package com.furkan.ecommerce.auth.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdatePaymentProfileRequest(
        @NotBlank
        @Size(max = 80)
        String firstName,
        @NotBlank
        @Size(max = 80)
        String lastName,
        @NotBlank
        @Pattern(regexp = "^\\+90\\d{10}$", message = "phoneNumber must be in +90XXXXXXXXXX format")
        String phoneNumber,
        @NotBlank
        @Pattern(regexp = "^\\d{11}$", message = "identityNumber must be 11 digits")
        String identityNumber,
        @NotBlank
        @Pattern(regexp = "^\\S+(?:\\s+\\S+)+$", message = "address must contain at least two words")
        @Size(max = 255)
        String address,
        @NotBlank
        @Size(max = 80)
        String city,
        @NotBlank
        @Size(max = 80)
        String country,
        @NotBlank
        @Pattern(regexp = "^\\d{5}$", message = "zipCode must be 5 digits")
        String zipCode
) {}

