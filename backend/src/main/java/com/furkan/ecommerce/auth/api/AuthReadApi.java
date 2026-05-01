package com.furkan.ecommerce.auth.api;

import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import java.util.Optional;

public interface AuthReadApi {
    Optional<AuthPaymentProfileView> findPaymentProfileById(Long userId);
}

