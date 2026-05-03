package com.furkan.ecommerce.auth.api;

import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.auth.api.dto.AuthUserSummaryView;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AuthReadApi {
    Optional<AuthPaymentProfileView> findPaymentProfileById(Long userId);
    default List<AuthUserSummaryView> findUserSummariesByIds(Collection<Long> userIds) {
        return List.of();
    }
    default List<Long> findUserIdsByEmailContaining(String email) {
        return List.of();
    }
}
