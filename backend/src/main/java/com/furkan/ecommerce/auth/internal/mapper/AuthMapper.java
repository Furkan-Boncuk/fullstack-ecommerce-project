package com.furkan.ecommerce.auth.internal.mapper;

import com.furkan.ecommerce.auth.api.dto.AuthPrincipalView;
import com.furkan.ecommerce.auth.api.dto.AuthPaymentProfileView;
import com.furkan.ecommerce.auth.api.dto.AuthResponse;
import com.furkan.ecommerce.auth.internal.application.AuthTokenResult;
import com.furkan.ecommerce.auth.internal.domain.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AuthMapper {
    AuthResponse toResponse(AuthTokenResult result);

    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet()))")
    AuthPrincipalView toPrincipalView(User user);

    @Mapping(target = "userId", source = "id")
    AuthPaymentProfileView toPaymentProfileView(User user);
}
