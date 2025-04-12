package com.simplesdental.product.controller.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(
    String token,
    Long id,
    String email,
    String role
) {
    @JsonProperty("type")
    public String type() {
        return "Bearer";
    }
}
