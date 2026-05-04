package com.furkan.ecommerce.auth.application;

import com.furkan.ecommerce.auth.AuthReadApi;
import com.furkan.ecommerce.auth.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.auth.dto.AuthUserSummaryView;
import com.furkan.ecommerce.auth.mapper.AuthMapper;
import com.furkan.ecommerce.auth.persistence.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class AuthReadService implements AuthReadApi {
    private final UserRepository userRepository;
    private final AuthMapper authMapper;

    @Override
    public Optional<AuthPaymentProfileView> findPaymentProfileById(Long userId) {
        return userRepository.findById(userId).map(authMapper::toPaymentProfileView);
    }

    @Override
    public List<AuthUserSummaryView> findUserSummariesByIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userRepository.findByIdIn(userIds).stream()
                .map(authMapper::toUserSummaryView)
                .toList();
    }

    @Override
    public List<Long> findUserIdsByEmailContaining(String email) {
        if (email == null || email.isBlank()) {
            return List.of();
        }
        return userRepository.findByEmailContainingIgnoreCase(email.trim()).stream()
                .map(user -> user.getId())
                .toList();
    }
}
