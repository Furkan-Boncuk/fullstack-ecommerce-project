package com.furkan.ecommerce.auth.dto;

import java.util.Set;

public record AuthPrincipalView(Long id, String email, Set<String> roles) {}
