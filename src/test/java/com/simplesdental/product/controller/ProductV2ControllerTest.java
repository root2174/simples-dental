package com.simplesdental.product.controller;

import com.simplesdental.product.controller.dto.product.v2.CreateProductV2DTO;
import com.simplesdental.product.controller.dto.product.v2.UpdateProductV2DTO;
import com.simplesdental.product.exception.BusinessException;
import com.simplesdental.product.exception.ResourceNotFoundException;
import com.simplesdental.product.model.Category;
import com.simplesdental.product.model.Product;
import com.simplesdental.product.service.ProductService;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductV2ControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductV2Controller productV2Controller;

    private Product product;
    private CreateProductV2DTO createProductV2DTO;
    private UpdateProductV2DTO updateProductV2DTO;
    private Pageable pageable;
    private UriComponentsBuilder uriBuilder;

    @BeforeEach
    void setUp() {
      Category category = Category.builder()
          .id(1L)
          .name("Test Category")
          .build();

        product = Product.builder()
            .id(1L)
            .name("Test Product")
            .description("Test Description")
            .price(new BigDecimal("100.00"))
            .status(true)
            .code(1)
            .category(category)
            .build();

        createProductV2DTO = new CreateProductV2DTO(
            "New Product",
            "New Description",
            new BigDecimal("200.00"),
            true,
            2,
            1L
        );

        updateProductV2DTO = UpdateProductV2DTO.builder()
            .name("Updated Product")
            .description("Updated Description")
            .price(new BigDecimal("300.00"))
            .status(false)
            .code(3)
            .categoryId(1L)
            .build();

        pageable = PageRequest.of(0, 10);
        uriBuilder = UriComponentsBuilder.newInstance();
    }

    @Test
    void shouldCreateProductSuccessfullyWhenValidDataIsProvided() {
        when(productService.save(any(CreateProductV2DTO.class))).thenReturn(product);

        ResponseEntity<Void> response = productV2Controller.createProduct(createProductV2DTO, uriBuilder);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(productService).save(createProductV2DTO);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileCreatingProduct() {
        when(productService.save(any(CreateProductV2DTO.class)))
            .thenThrow(new BusinessException("Error creating product"));

        assertThrows(BusinessException.class, () -> productV2Controller.createProduct(createProductV2DTO, uriBuilder));
        verify(productService).save(createProductV2DTO);
    }

    @Test
    void shouldUpdateProductSuccessfullyWhenValidDataIsProvided() {
        when(productService.update(anyLong(), any(UpdateProductV2DTO.class))).thenReturn(updateProductV2DTO);

        ResponseEntity<UpdateProductV2DTO> response = productV2Controller.updateProduct(1L, updateProductV2DTO);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updateProductV2DTO, response.getBody());
        verify(productService).update(1L, updateProductV2DTO);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentProduct() {
        when(productService.update(anyLong(), any(UpdateProductV2DTO.class)))
            .thenThrow(new ResourceNotFoundException("Product", "id", 1L));

        assertThrows(ResourceNotFoundException.class, () -> 
            productV2Controller.updateProduct(1L, updateProductV2DTO));
        verify(productService).update(1L, updateProductV2DTO);
    }

    @Test
    void shouldDeleteProductSuccessfullyWhenValidIdIsProvided() {
        when(productService.findById(any())).thenReturn(Optional.of(product));
        doNothing().when(productService).deleteById(anyLong());

        ResponseEntity<Void> response = productV2Controller.deleteProduct(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productService).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentProduct() {
        when(productService.findById(any())).thenReturn(Optional.empty());
        ResponseEntity<Void> response = productV2Controller.deleteProduct(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldReturnPageOfProductsWhenRequestingAllProducts() {
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
        when(productService.findAll(any(Pageable.class))).thenReturn(productPage);

        ResponseEntity<Page<Product>> response = productV2Controller.getAllProducts(pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productPage, response.getBody());
        verify(productService).findAll(pageable);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileFetchingProducts() {
        when(productService.findAll(any(Pageable.class)))
            .thenThrow(new BusinessException("Database error"));

        assertThrows(BusinessException.class, () -> productV2Controller.getAllProducts(pageable));
        verify(productService).findAll(pageable);
    }
} 
