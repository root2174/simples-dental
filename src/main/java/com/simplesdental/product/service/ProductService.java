package com.simplesdental.product.service;

import com.simplesdental.product.controller.dto.product.v1.CreateProductDTO;
import com.simplesdental.product.controller.dto.product.v1.UpdateProductDTO;
import com.simplesdental.product.controller.dto.product.v2.CreateProductV2DTO;
import com.simplesdental.product.controller.dto.product.v2.UpdateProductV2DTO;
import com.simplesdental.product.model.Product;
import com.simplesdental.product.repository.CategoryRepository;
import com.simplesdental.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<Product> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }


    @SneakyThrows
    @Transactional
    public Product save(CreateProductV2DTO input) {
        var category = categoryRepository.findById(input.categoryId()).orElse(null);

        if (category == null) {
            throw new ClassNotFoundException("A categoria informada não existe.");
        }

        var product =
            Product.builder()
                .name(input.name())
                .description(input.description())
                .price(input.price())
                .status(input.status())
                .code(input.code())
                .category(category)
                .build();

        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @SneakyThrows
    @Transactional
    public UpdateProductV2DTO update(Long id, @Valid UpdateProductV2DTO input) {
        var product = findById(id);

        if (product.isEmpty()) {
            throw new ClassNotFoundException("Produto não encontrado.");
        }

        var category = product.get().getCategory();

        if (input.categoryId() != null) {
            category = categoryRepository.findById(input.categoryId()).orElse(null);

            if (category == null) {
                throw new ClassNotFoundException("Categoria não encontrada.");
            }
        }

        var updatedProduct = product.get().update(input, category);

        var savedProduct = productRepository.save(updatedProduct);

        return UpdateProductV2DTO.builder()
            .name(savedProduct.getName())
            .description(savedProduct.getDescription())
            .price(savedProduct.getPrice())
            .status(savedProduct.getStatus())
            .code(savedProduct.getCode())
            .build();

    }

    @Transactional
    public UpdateProductDTO update(Long id, @Valid UpdateProductDTO input) {
        var code = getCodeAsInteger(input.code());

        var updatedProduct = this.update(id,
            UpdateProductV2DTO
                .builder()
                .name(input.name())
                .description(input.description())
                .price(input.price())
                .status(input.status())
                .code(code)
                .categoryId(input.categoryId())
                .build());

        return UpdateProductDTO.builder()
            .name(updatedProduct.name())
            .description(updatedProduct.description())
            .price(updatedProduct.price())
            .status(updatedProduct.status())
            .code("PROD-" + code)
            .categoryId(updatedProduct.categoryId())
            .build();
    }


        @SneakyThrows
    private Integer getCodeAsInteger(String code) {
        if (code == null) {
            return null;
        }

        if (!code.matches("^PROD-\\d+$")) {
            throw new IllegalAccessException("Produto não possui o código no formato esperado. ex: PROD-001");
        }

        return Integer.parseInt(code.replaceAll("\\D", ""));
    }

    @Transactional
    public Product save(@Valid CreateProductDTO input) {
        var code = getCodeAsInteger(input.code());

        return this.save(new CreateProductV2DTO(
            input.name(),
            input.description(),
            input.price(),
            input.status(),
            code,
            input.categoryId()));
    }
}