package com.simplesdental.product.controller.dto.product.v1;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record UpdateProductDTO(
    String name,
    String description,
    BigDecimal price,
    Boolean status,
    String code,
    Long categoryId) {}
