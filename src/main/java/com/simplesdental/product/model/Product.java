package com.simplesdental.product.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simplesdental.product.controller.dto.product.v2.UpdateProductV2DTO;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder(toBuilder = true)
@Setter
@Entity
@Table(name = "products")
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean status;
    private Integer code;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"products"})
    private Category category;

    public Product update(@Valid UpdateProductV2DTO input, Category categoryInput) {
        if (input.name() != null && !input.name().equals(name)) {
            name = input.name();
        }

        if (input.description() != null && !input.description().equals(description)) {
            description = input.description();
        }

        if (input.price() != null && !input.price().equals(price)) {
            price = input.price();
        }

        if (input.status() != null && !input.status().equals(status)) {
            status = input.status();
        }

        if (input.code() != null && !input.code().equals(code)) {
            code = input.code();
        }

        category = categoryInput;

        return this;
    }
}