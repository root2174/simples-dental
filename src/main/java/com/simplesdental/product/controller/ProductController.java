package com.simplesdental.product.controller;

import com.simplesdental.product.controller.dto.product.v1.CreateProductDTO;
import com.simplesdental.product.controller.dto.product.v1.UpdateProductDTO;
import com.simplesdental.product.model.Product;
import com.simplesdental.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "Product Management V1", description = "APIs for managing products.")
@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService productService;

  @Autowired
  public ProductController(ProductService productService) {
      this.productService = productService;
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
  @Transactional
  public ResponseEntity<Page<Product>> getAllProducts(Pageable pageable) {
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
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
      return productService.findById(id)
          .map(product -> {
                if (product.getCategory() != null) {
                    Hibernate.initialize(product.getCategory());
                }
                return ResponseEntity.ok(product);
            })
            .orElse(ResponseEntity.notFound().build());
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
  public ResponseEntity<String> createProduct(@Valid @RequestBody CreateProductDTO input,
      UriComponentsBuilder uriBuilder) {
    var savedProduct = productService.save(input);

    return ResponseEntity.created(
        uriBuilder
            .path("/api/products/{id}")
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
              schema = @Schema(implementation = UpdateProductDTO.class))),
      @ApiResponse(responseCode = "404", description = "Product not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
  })
  @PutMapping("/{id}")
  public ResponseEntity<UpdateProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductDTO product) {
      var updatedProduct = productService.update(id, product);
      return ResponseEntity.ok(updatedProduct);
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
  public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
      return productService.findById(id)
              .map(product -> {
                  productService.deleteById(id);
                  return ResponseEntity.noContent().<Void>build();
              })
              .orElse(ResponseEntity.notFound().build());
  }
}