package com.simplesdental.product.controller;

import com.simplesdental.product.controller.dto.product.v2.CreateProductV2DTO;
import com.simplesdental.product.controller.dto.product.v2.UpdateProductV2DTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
@Tag(name = "Product Management V2", description = "APIs for managing products.")
public class ProductV2Controller {

  private final ProductService productService;
  private final LoggerWrapper logger = new LoggerWrapper(ProductV2Controller.class);

  @Operation(summary = "Create a product")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Product successfully created", content = {
          @Content(schema = @Schema(hidden = true))
      }),
      @ApiResponse(responseCode = "404", description = "A categoria informada não existe."),
      @ApiResponse(responseCode = "500", description = "Erro Interno")
  })
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> createProduct(@Valid @RequestBody CreateProductV2DTO input,
      UriComponentsBuilder uriBuilder) {
    logger.info("Creating a product with name {}", input.name());
    var savedProduct = productService.save(input);

    return ResponseEntity.created(
            uriBuilder
                .path("/api/v2/products/{id}")
                .buildAndExpand(savedProduct.getId())
                .toUri())
        .build();
  }


  @Operation(
      summary = "Update a product",
      description = "Updates a product identified by its ID using the provided update data."
  )
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UpdateProductV2DTO.class))),
      @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
  })
  @PreAuthorize("hasRole('ADMIN')")
  @PutMapping("/{id}")
  public ResponseEntity<UpdateProductV2DTO> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductV2DTO input) {
    logger.info("Updating product with id {}", id);
    var updatedProduct = productService.update(id, input);
    return ResponseEntity.ok(updatedProduct);
  }


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
  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
  public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
    logger.info("Getting all products...");
    Page<Product> products = productService.findAll(pageable);
    products.forEach(product -> {
      if (product.getCategory() != null) {
        Hibernate.initialize(product.getCategory());
      }
    });

    return ResponseEntity.ok(products);
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
    logger.info("Getting product with id {}", id);
    return productService.findById(id)
        .map(product -> {
          if (product.getCategory() != null) {
            Hibernate.initialize(product.getCategory());
          }
          return ResponseEntity.ok(product);
        })
        .orElse(ResponseEntity.notFound().build());
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
    logger.info("Deleting product with id {}", id);
    return productService.findById(id)
        .map(product -> {
          productService.deleteById(id);
          return ResponseEntity.noContent().<Void>build();
        })
        .orElse(ResponseEntity.notFound().build());
  }
}
