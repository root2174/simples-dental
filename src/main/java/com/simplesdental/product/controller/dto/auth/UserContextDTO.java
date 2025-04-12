package com.simplesdental.product.controller.dto.auth;

import com.simplesdental.product.model.User;
import lombok.Builder;

@Builder
public record UserContextDTO(
    Long id,
    String email,
    String role
) {
    public static UserContextDTO fromUser(User user) {
        return UserContextDTO.builder()
            .id(user.getId())
            .email(user.getEmail())
            .role(user.getRole().name())
            .build();
    }
} 
