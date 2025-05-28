package com.canvamedium.service.impl;

import com.canvamedium.model.Category;
import com.canvamedium.repository.CategoryRepository;
import com.canvamedium.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the CategoryService interface.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    /**
     * Constructor with dependencies injection.
     *
     * @param categoryRepository The category repository
     */
    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    public Optional<Category> getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        // Validate name and slug uniqueness
        validateCategoryNameAndSlug(category);
        
        // Set timestamps
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        // Only validate name and slug if they are being changed
        if (!category.getName().equals(categoryDetails.getName())) {
            if (!isCategoryNameAvailable(categoryDetails.getName())) {
                throw new IllegalArgumentException("Category name already exists: " + categoryDetails.getName());
            }
            category.setName(categoryDetails.getName());
        }
        
        if (!category.getSlug().equals(categoryDetails.getSlug())) {
            if (!isCategorySlugAvailable(categoryDetails.getSlug())) {
                throw new IllegalArgumentException("Category slug already exists: " + categoryDetails.getSlug());
            }
            category.setSlug(categoryDetails.getSlug());
        }
        
        // Update other fields
        category.setDescription(categoryDetails.getDescription());
        category.setIcon(categoryDetails.getIcon());
        category.setColor(categoryDetails.getColor());
        category.setFeatured(categoryDetails.isFeatured());
        category.setParentId(categoryDetails.getParentId());
        category.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        // Check if the category has child categories
        List<Category> childCategories = categoryRepository.findByParentId(id);
        if (!childCategories.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with child categories");
        }
        
        categoryRepository.delete(category);
    }

    @Override
    public Page<Category> searchCategories(String query, Pageable pageable) {
        return categoryRepository.findByNameContainingIgnoreCase(query, pageable);
    }

    @Override
    public Page<Category> getFeaturedCategories(Pageable pageable) {
        return categoryRepository.findByFeaturedTrue(pageable);
    }

    @Override
    public List<Category> getChildCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByParentIdIsNull();
    }

    @Override
    public Page<Category> getPopularCategories(Pageable pageable) {
        return categoryRepository.findCategoriesByPopularity(pageable);
    }

    @Override
    @Transactional
    public Category setFeaturedStatus(Long id, boolean featured) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        
        category.setFeatured(featured);
        category.setUpdatedAt(LocalDateTime.now());
        
        return categoryRepository.save(category);
    }

    @Override
    public boolean isCategoryNameAvailable(String name) {
        return !categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    public boolean isCategorySlugAvailable(String slug) {
        return !categoryRepository.existsBySlug(slug);
    }

    /**
     * Validates the uniqueness of the category name and slug.
     *
     * @param category The category to validate
     * @throws IllegalArgumentException if the name or slug is already in use
     */
    private void validateCategoryNameAndSlug(Category category) {
        if (!isCategoryNameAvailable(category.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + category.getName());
        }
        
        if (!isCategorySlugAvailable(category.getSlug())) {
            throw new IllegalArgumentException("Category slug already exists: " + category.getSlug());
        }
    }
} 