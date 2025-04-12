package com.simplesdental.product.service;

import com.simplesdental.product.controller.dto.product.v1.CreateProductDTO;
import com.simplesdental.product.controller.dto.product.v1.UpdateProductDTO;
import com.simplesdental.product.controller.dto.product.v2.CreateProductV2DTO;
import com.simplesdental.product.controller.dto.product.v2.UpdateProductV2DTO;
import com.simplesdental.product.exception.BusinessException;
import com.simplesdental.product.exception.ResourceNotFoundException;
import com.simplesdental.product.logging.LoggerWrapper;
import com.simplesdental.product.model.Product;
import com.simplesdental.product.repository.CategoryRepository;
import com.simplesdental.product.repository.ProductRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final LoggerWrapper logger = new LoggerWrapper(ProductService.class);

    public Page<Product> findAll(Pageable pageable) {
        logger.info("Retrieving all products");
        try {
            Page<Product> products = productRepository.findAll(pageable);
            logger.info("Successfully retrieved {} products", products.getTotalElements());
            return products;
        } catch (Exception e) {
            logger.error("Error retrieving products: {}", e.getMessage());
            throw e;
        }
    }

    public Optional<Product> findById(Long id) {
        logger.info("Retrieving product with id: {}", id);
        try {
            return productRepository.findById(id);
        } catch (Exception e) {
            logger.error("Error retrieving product with id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @SneakyThrows
    @Transactional
    public Product save(CreateProductV2DTO input) {
        logger.info("Saving new product: {}", input.name());
        var category = categoryRepository.findById(input.categoryId()).orElse(null);

        if (category == null) {
            throw new ResourceNotFoundException("A categoria informada não existe.");
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

        try {
            Product savedProduct = productRepository.save(product);
            logger.info("Product saved successfully with id: {}", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            logger.error("Error saving product {}: {}", input.name(), e.getMessage());
            throw e;
        }
    }

    public void deleteById(Long id) {
        logger.info("Deleting product with id: {}", id);
        try {
            productRepository.deleteById(id);
            logger.info("Product deleted successfully with id: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting product with id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @SneakyThrows
    @Transactional
    public UpdateProductV2DTO update(Long id, @Valid UpdateProductV2DTO input) {
        logger.info("Updating product with id: {}", id);
        var product = findById(id);

        if (product.isEmpty()) {
            logger.warn("product with id: {} not found", id);
            throw new ResourceNotFoundException("Produto não encontrado.");
        }

        var category = product.get().getCategory();

        if (input.categoryId() != null) {
            category = categoryRepository.findById(input.categoryId()).orElse(null);

            if (category == null) {
                logger.warn("category with id {} not found.", input.categoryId());
                throw new ResourceNotFoundException("Categoria não encontrada.");
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
            .categoryId(savedProduct.getCategory().getId())
            .build();
    }

    @Transactional
    public UpdateProductDTO update(Long id, @Valid UpdateProductDTO input) {
        logger.info("Updating product with id: {}", id);
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

    @Transactional
    public Product save(@Valid CreateProductDTO input) {
        logger.info("Saving new product: {}", input.name());
        var code = getCodeAsInteger(input.code());

        return this.save(new CreateProductV2DTO(
            input.name(),
            input.description(),
            input.price(),
            input.status(),
            code,
            input.categoryId()));
    }

    @SneakyThrows
    private Integer getCodeAsInteger(String code) {
        if (code == null) {
            return null;
        }

        if (!code.matches("^PROD-\\d+$")) {
            logger.warn("Invalid code: {}", code);
            throw new BusinessException("Produto não possui o código no formato esperado. ex: PROD-001");
        }

        return Integer.parseInt(code.replaceAll("\\D", ""));
    }
}
