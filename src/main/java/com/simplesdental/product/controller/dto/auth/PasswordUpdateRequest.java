package com.simplesdental.product.controller.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record PasswordUpdateRequest(
    @NotBlank(message = "New password is required")
    String newPassword
) {}
