package com.simplesdental.product.controller;

import com.simplesdental.product.controller.dto.product.v1.CreateProductDTO;
import com.simplesdental.product.controller.dto.product.v1.UpdateProductDTO;
import com.simplesdental.product.logging.LoggerWrapper;
import com.simplesdental.product.model.Product;
import com.simplesdental.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Product Management V1", description = "APIs for managing products.")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final LoggerWrapper logger = new LoggerWrapper(ProductController.class);

    @Operation(
        summary = "Get all products",
        description = "Retrieves a paginated list of products. For each product, if a category exists it is eagerly initialized."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @GetMapping
    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
        logger.info("Received request to get all products");
        try {
            Page<Product> products = productService.findAll(pageable);
            products.forEach(product -> {
                if (product.getCategory() != null) {
                    Hibernate.initialize(product.getCategory());
                }
            });
            logger.info("Successfully returned {} products", products.getTotalElements());
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error getting all products: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get product by ID",
        description = "Retrieves the product with the specified ID. Returns a 404 if the product is not found."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Product.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.info("Received request to get product with id: {}", id);
        try {
            return productService.findById(id)
                .map(product -> {
                    if (product.getCategory() != null) {
                        Hibernate.initialize(product.getCategory());
                    }
                    logger.info("Successfully returned product with id: {}", id);
                    return ResponseEntity.ok(product);
                })
                .orElseGet(() -> {
                    logger.warn("Product not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
        } catch (Exception e) {
            logger.error("Error getting product with id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "Create a product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product successfully created", content = {
            @Content(schema = @Schema(hidden = true))
        }),
        @ApiResponse(responseCode = "404", description = "A categoria informada não existe."),
        @ApiResponse(responseCode = "500", description = "Erro Interno")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createProduct(@Valid @RequestBody CreateProductDTO input,
        UriComponentsBuilder uriBuilder) {
        logger.info("Received request to create product with name: {}", input.name());
        try {
            var savedProduct = productService.save(input);
            logger.info("Successfully created product with id: {}", savedProduct.getId());
            return ResponseEntity.created(
                uriBuilder
                    .path("/api/products/{id}")
                    .buildAndExpand(savedProduct.getId())
                    .toUri())
                .build();
        } catch (Exception e) {
            logger.error("Error creating product with name {}: {}", input.name(), e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Update a product",
        description = "Updates a product identified by its ID using the provided update data."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UpdateProductDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UpdateProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductDTO product) {
        logger.info("Received request to update product with id: {}", id);
        try {
            var updatedProduct = productService.update(id, product);
            logger.info("Successfully updated product with id: {}", id);
            return ResponseEntity.ok(updatedProduct);
        } catch (Exception e) {
            logger.error("Error updating product with id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Delete a product",
        description = "Deletes the product with the specified ID. Returns a 404 if no product is found."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Received request to delete product with id: {}", id);
        try {
            productService.deleteById(id);
            logger.info("Successfully deleted product with id: {}", id);
            return ResponseEntity.noContent().<Void>build();
        } catch (Exception e) {
            logger.error("Error deleting product with id {}: {}", id, e.getMessage());
            throw e;
        }
    }
}
