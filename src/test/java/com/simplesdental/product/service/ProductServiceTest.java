package com.simplesdental.product.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.simplesdental.product.controller.dto.product.v1.CreateProductDTO;
import com.simplesdental.product.controller.dto.product.v1.UpdateProductDTO;
import com.simplesdental.product.controller.dto.product.v2.CreateProductV2DTO;
import com.simplesdental.product.controller.dto.product.v2.UpdateProductV2DTO;
import com.simplesdental.product.exception.BusinessException;
import com.simplesdental.product.exception.ResourceNotFoundException;
import com.simplesdental.product.model.Category;
import com.simplesdental.product.model.Product;
import com.simplesdental.product.repository.CategoryRepository;
import com.simplesdental.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("100.00"));
        product.setStatus(true);
        product.setCode(1);
        product.setCategory(category);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void shouldReturnPageOfProductsWhenRequestingAllProducts() {
        List<Product> products = Collections.singletonList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());
        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        Page<Product> result = productService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(product, result.getContent().get(0));
        verify(productRepository).findAll(pageable);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileFetchingProducts() {
        when(productRepository.findAll(any(Pageable.class)))
            .thenThrow(new BusinessException("Database error"));

        assertThrows(BusinessException.class, () -> productService.findAll(pageable));
        verify(productRepository).findAll(pageable);
    }

    @Test
    void shouldReturnProductWhenRequestingById() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        Optional<Product> result = productService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(product, result.get());
        verify(productRepository).findById(1L);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileFindingProductById() {
        when(productRepository.findById(anyLong()))
            .thenThrow(new BusinessException("Database error"));

        assertThrows(BusinessException.class, () -> productService.findById(1L));
        verify(productRepository).findById(1L);
    }

    @Test
    void shouldCreateProductSuccessfullyWhenValidDataIsProvided() {
        CreateProductV2DTO input = new CreateProductV2DTO(
            "New Product",
            "New Description",
            new BigDecimal("200.00"),
            true,
            2,
            1L
        );
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.save(input);

        assertNotNull(result);
        assertEquals(product, result);
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCreatingProductWithNonExistentCategory() {
        CreateProductV2DTO input = new CreateProductV2DTO(
            "New Product",
            "New Description",
            new BigDecimal("200.00"),
            true,
            2,
            1L
        );
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.save(input));
        verify(categoryRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldCreateProductV1SuccessfullyWhenValidDataIsProvided() {
        CreateProductDTO input = new CreateProductDTO(
            "New Product",
            "New Description",
            new BigDecimal("200.00"),
            true,
            "PROD-002",
            1L
        );
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.save(input);

        assertNotNull(result);
        assertEquals(product, result);
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowBusinessExceptionWhenCreatingProductV1WithInvalidCodeFormat() {
        CreateProductDTO input = new CreateProductDTO(
            "New Product",
            "New Description",
            new BigDecimal("200.00"),
            true,
            "INVALID-CODE",
            1L
        );

        assertThrows(BusinessException.class, () -> productService.save(input));
        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldUpdateProductV2SuccessfullyWhenValidDataIsProvided() {
        UpdateProductV2DTO input = UpdateProductV2DTO.builder()
            .name("Updated Product")
            .description("Updated Description")
            .price(new BigDecimal("300.00"))
            .status(false)
            .code(3)
            .build();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        UpdateProductV2DTO result = productService.update(1L, input);

        assertNotNull(result);
        assertEquals(input.name(), result.name());
        assertEquals(input.description(), result.description());
        assertEquals(input.price(), result.price());
        assertEquals(input.status(), result.status());
        assertEquals(input.code(), result.code());
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentProduct() {
        UpdateProductV2DTO input = UpdateProductV2DTO.builder()
            .name("Updated Product")
            .description("Updated Description")
            .price(new BigDecimal("300.00"))
            .status(false)
            .code(3)
            .build();
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.update(1L, input));
        verify(productRepository).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void shouldUpdateProductV1SuccessfullyWhenValidDataIsProvided() {
        UpdateProductDTO input = UpdateProductDTO.builder()
            .name("Updated Product")
            .description("Updated Description")
            .price(new BigDecimal("300.00"))
            .status(false)
            .code("PROD-3")
            .categoryId(1L)
            .build();
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        UpdateProductDTO result = productService.update(1L, input);

        assertNotNull(result);
        assertEquals(input.name(), result.name());
        assertEquals(input.description(), result.description());
        assertEquals(input.price(), result.price());
        assertEquals(input.status(), result.status());
        assertEquals(input.code(), result.code());
        assertEquals(input.categoryId(), result.categoryId());
        verify(productRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldDeleteProductSuccessfullyWhenValidIdIsProvided() {
        doNothing().when(productRepository).deleteById(anyLong());

        productService.deleteById(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void shouldThrowRuntimeExceptionWhenErrorOccursWhileDeletingProduct() {
        doThrow(new RuntimeException("Database error")).when(productRepository).deleteById(anyLong());

        assertThrows(RuntimeException.class, () -> productService.deleteById(1L));
    }
} 
