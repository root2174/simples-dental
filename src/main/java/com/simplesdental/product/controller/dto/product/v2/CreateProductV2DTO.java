package com.simplesdental.product.controller.dto.product.v2;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CreateProductV2DTO(
    @NotBlank(message = "O nome do produto é obrigatório e não pode estar em branco.")
    @Size(max = 100, message = "O nome do produto deve ter no máximo 100 caracteres.")
    String name,

    @Size(max = 255, message = "A descrição do produto deve ter no máximo 255 caracteres.")
    String description,

    @NotNull(message = "O preço do produto é obrigatório.")
    @Positive(message = "O preço do produto deve ser maior que zero.")
    BigDecimal price,

    @NotNull(message = "O status do produto é obrigatório.")
    Boolean status,

    Integer code,

    @NotNull(message = "O id da categoria do produto é obrigatória.")
    Long categoryId) {}
