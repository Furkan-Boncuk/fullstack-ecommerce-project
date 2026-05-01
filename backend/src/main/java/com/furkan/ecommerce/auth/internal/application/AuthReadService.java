package com.furkan.ecommerce.auth.internal.application;

import com.furkan.ecommerce.auth.api.AuthReadApi;
import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.auth.internal.mapper.AuthMapper;
import com.furkan.ecommerce.auth.internal.persistence.UserRepository;
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
}

