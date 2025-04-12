package com.simplesdental.product.service;

import com.simplesdental.product.exception.BusinessException;
import com.simplesdental.product.exception.ResourceNotFoundException;
import com.simplesdental.product.model.Category;
import com.simplesdental.product.repository.CategoryRepository;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    private Category category;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository);
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
        when(categoryRepository.findAll(any(Pageable.class))).thenReturn(categoryPage);

        Page<Category> result = categoryService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(category, result.getContent().get(0));
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileFetchingCategories() {
        when(categoryRepository.findAll(any(Pageable.class)))
            .thenThrow(new RuntimeException("Database error"));

        assertThrows(BusinessException.class, () -> categoryService.findAll(pageable));
        verify(categoryRepository).findAll(pageable);
    }

    @Test
    void shouldReturnCategoryWhenRequestingById() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));

        Category result = categoryService.findById(1L);

        assertNotNull(result);
        assertEquals(category, result);
        verify(categoryRepository).findById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> categoryService.findById(1L));
        verify(categoryRepository).findById(1L);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileFindingCategoryById() {
        when(categoryRepository.findById(anyLong()))
            .thenThrow(new RuntimeException("Database error"));

        assertThrows(BusinessException.class, () -> categoryService.findById(1L));
        verify(categoryRepository).findById(1L);
    }

    @Test
    void shouldCreateCategorySuccessfullyWhenValidDataIsProvided() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.save(category);

        assertNotNull(result);
        assertEquals(category, result);
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileCreatingCategory() {
        when(categoryRepository.save(any(Category.class)))
            .thenThrow(new RuntimeException("Database error"));

        assertThrows(BusinessException.class, () -> categoryService.save(category));
        verify(categoryRepository).save(category);
    }

    @Test
    void shouldDeleteCategorySuccessfullyWhenValidIdIsProvided() {
        when(categoryRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(anyLong());

        categoryService.deleteById(1L);

        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistentCategory() {
        when(categoryRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> categoryService.deleteById(1L));
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldThrowBusinessExceptionWhenErrorOccursWhileDeletingCategory() {
        when(categoryRepository.existsById(anyLong())).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(categoryRepository).deleteById(anyLong());

        assertThrows(BusinessException.class, () -> categoryService.deleteById(1L));
        verify(categoryRepository).existsById(1L);
        verify(categoryRepository).deleteById(1L);
    }
} 
