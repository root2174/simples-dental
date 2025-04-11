package com.simplesdental.product.controller.dto.product.v2;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpdateProductV2DTO(
    String name,
    String description,
    BigDecimal price,
    Boolean status,
    Integer code,
    Long categoryId) {}
