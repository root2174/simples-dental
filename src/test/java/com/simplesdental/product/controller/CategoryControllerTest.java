package com.simplesdental.product.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.simplesdental.product.exception.BusinessException;
import com.simplesdental.product.exception.ResourceNotFoundException;
import com.simplesdental.product.model.Category;
import com.simplesdental.product.service.CategoryService;
import java.util.Collections;
import java.util.List;
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

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private Category category;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        category = Category.builder()
            .id(1L)
            .name("Test Category")
            .build();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void shouldReturnPageOfCategoriesWhenRequestingAllCategories() {
        List<Category> categories = Collections.singletonList(category);
        Page<Category> categoryPage = new PageImpl<>(categories, pageable, categories.size());
        when(categoryService.findAll(any(Pageable.class))).thenReturn(categoryPage);

        ResponseEntity<Page<Category>> response = categoryController.getAllCategories(pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categoryPage, response.getBody());
        verify(categoryService).findAll(pageable);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileFetchingCategories() {
        when(categoryService.findAll(any(Pageable.class)))
            .thenThrow(new BusinessException("Database error"));

        assertThrows(BusinessException.class, () -> categoryController.getAllCategories(pageable));
        verify(categoryService).findAll(pageable);
    }

    @Test
    void shouldReturnCategoryWhenRequestingById() {
        when(categoryService.findById(anyLong())).thenReturn(category);

        ResponseEntity<Category> response = categoryController.getCategoryById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(category, response.getBody());
        verify(categoryService).findById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCategoryNotFound() {
        when(categoryService.findById(anyLong()))
            .thenThrow(new ResourceNotFoundException("Category", "id", 1L));

        assertThrows(ResourceNotFoundException.class, () -> categoryController.getCategoryById(1L));
        verify(categoryService).findById(1L);
    }

    @Test
    void shouldCreateCategoryWhenValidDataIsProvided() {
        when(categoryService.save(any(Category.class))).thenReturn(category);

        ResponseEntity<Category> response = categoryController.createCategory(category);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(category, response.getBody());
        verify(categoryService).save(category);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileCreatingCategory() {
        when(categoryService.save(any(Category.class)))
            .thenThrow(new BusinessException("Error creating category"));

        assertThrows(BusinessException.class, () -> categoryController.createCategory(category));
        verify(categoryService).save(category);
    }

    @Test
    void shouldUpdateCategoryWhenValidDataIsProvided() {
        when(categoryService.findById(anyLong())).thenReturn(category);

        ResponseEntity<Category> response = categoryController.updateCategory(1L, category);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(category, response.getBody());
        verify(categoryService).findById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenUpdatingNonExistentCategory() {
        when(categoryService.findById(anyLong()))
            .thenThrow(new ResourceNotFoundException("Category", "id", 1L));

        assertThrows(ResourceNotFoundException.class, () -> 
            categoryController.updateCategory(1L, category));
        verify(categoryService).findById(1L);
    }

    @Test
    void shouldDeleteCategoryWhenValidIdIsProvided() {
        doNothing().when(categoryService).deleteById(anyLong());

        ResponseEntity<Void> response = categoryController.deleteCategory(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(categoryService).deleteById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentCategory() {
        doThrow(new ResourceNotFoundException("Category", "id", 1L))
            .when(categoryService).deleteById(anyLong());

        assertThrows(ResourceNotFoundException.class, () -> categoryController.deleteCategory(1L));
        verify(categoryService).deleteById(1L);
    }
} 
