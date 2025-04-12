package com.simplesdental.product.controller;

import com.simplesdental.product.logging.LoggerWrapper;
import com.simplesdental.product.model.Category;
import com.simplesdental.product.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Category Management", description = "APIs for managing categories.")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final LoggerWrapper logger = new LoggerWrapper(CategoryController.class);

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get all categories", description = "Retrieve all categories with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    public ResponseEntity<Page<Category>> getAllCategories(
            @PageableDefault(sort = {"name"}, direction = Sort.Direction.ASC) Pageable pageable) {
        logger.info("Received request to get all categories with pagination - page: {}, size: {}, sort: {}", 
            pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        try {
            Page<Category> categories = categoryService.findAll(pageable);
            logger.info("Successfully returned {} categories", categories.getTotalElements());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error getting all categories: {}", e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Get category by ID",
        description = "Retrieves a category by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category retrieved successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        logger.info("Received request to get category with id: {}", id);
        try {
            Category category = categoryService.findById(id);
            logger.info("Successfully returned category with id: {}", id);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            logger.error("Error getting category with id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Create category",
        description = "Creates a new category."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Category created successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        logger.info("Received request to create category: {}", category.getName());
        try {
            Category createdCategory = categoryService.save(category);
            logger.info("Successfully created category with id: {}", createdCategory.getId());
            return ResponseEntity.ok(createdCategory);
        } catch (Exception e) {
            logger.error("Error creating category {}: {}", category.getName(), e.getMessage());
            throw e;
        }
    }

    @Operation(
        summary = "Update category",
        description = "Updates an existing category by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Category updated successfully",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Category.class))),
        @ApiResponse(responseCode = "404", description = "Category not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @Valid @RequestBody Category category) {
        logger.info("Received request to update category with id: {}", id);
        var res = categoryService.findById(id);
        logger.info("Successfully updated category with id: {}", id);
        return ResponseEntity.ok(res);
    }

    @Operation(
        summary = "Delete category",
        description = "Deletes a category by its ID."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Category deleted successfully", content = @Content),
        @ApiResponse(responseCode = "404", description = "Category not found", content = @Content)
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        logger.info("Received request to delete category with id: {}", id);
        try {
            categoryService.deleteById(id);
            logger.info("Successfully deleted category with id: {}", id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting category with id {}: {}", id, e.getMessage());
            throw e;
        }
    }
}
