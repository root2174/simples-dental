package com.simplesdental.product.service;

import com.simplesdental.product.exception.BusinessException;
import com.simplesdental.product.exception.ResourceNotFoundException;
import com.simplesdental.product.logging.LoggerWrapper;
import com.simplesdental.product.model.Category;
import com.simplesdental.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final LoggerWrapper logger = new LoggerWrapper(CategoryService.class);

    public Page<Category> findAll(Pageable pageable) {
        logger.info("Retrieving all categories with pagination - page: {}, size: {}, sort: {}", 
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Category> categories = categoryRepository.findAll(pageable);
            logger.info("Successfully retrieved {} categories", categories.getTotalElements());
            return categories;
        } catch (Exception e) {
            logger.error("Error retrieving categories: {}", e.getMessage());
            throw new BusinessException("Error retrieving categories", e);
        }
    }

    public Category findById(Long id) {
        logger.info("Retrieving category with id: {}", id);
        try {
            return categoryRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Category not found with id: {}", id);
                    return new ResourceNotFoundException("Category", "id", id);
                });
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error retrieving category with id {}: {}", id, e.getMessage());
            throw new BusinessException("Error retrieving category", e);
        }
    }

    public Category save(Category category) {
        logger.info("Saving new category: {}", category.getName());
        try {
            Category savedCategory = categoryRepository.save(category);
            logger.info("Category saved successfully with id: {}", savedCategory.getId());
            return savedCategory;
        } catch (Exception e) {
            logger.error("Error saving category {}: {}", category.getName(), e.getMessage());
            throw new BusinessException("Error saving category", e);
        }
    }

    public void deleteById(Long id) {
        logger.info("Deleting category with id: {}", id);
        try {
            if (!categoryRepository.existsById(id)) {
                logger.warn("Category not found with id: {}", id);
                throw new ResourceNotFoundException("Category", "id", id);
            }
            categoryRepository.deleteById(id);
            logger.info("Category deleted successfully with id: {}", id);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error deleting category with id {}: {}", id, e.getMessage());
            throw new BusinessException("Error deleting category", e);
        }
    }
}
