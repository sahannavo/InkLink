package com.project.inklink.service;

import com.project.inklink.entity.Category;
import com.project.inklink.entity.Story;
import com.project.inklink.repository.CategoryRepository;
import com.project.inklink.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private StoryRepository storyRepository;

    @PostConstruct
    public void initDefaultCategories() {
        List<String> defaultCategories = Arrays.asList(
                "Fiction", "Non-Fiction", "Poetry", "Mystery",
                "Romance", "Science Fiction", "Fantasy", "Horror",
                "Biography", "History", "Self-Help", "Travel"
        );

        for (String categoryName : defaultCategories) {
            if (!categoryRepository.existsByName(categoryName)) {
                Category category = new Category();
                category.setName(categoryName);
                category.setDescription("Stories in the " + categoryName + " genre");
                categoryRepository.save(category);
            }
        }
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllOrderByName();
    }

    public Page<Story> getStoriesByCategory(String categoryName, Pageable pageable) {
        return storyRepository.findByCategory(categoryName, pageable);
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    // Alias method for consistency with the second version
    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }

    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
        }
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, Category categoryDetails) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Check if name is being changed and if it conflicts
            if (!category.getName().equals(categoryDetails.getName()) &&
                    categoryRepository.existsByName(categoryDetails.getName())) {
                throw new IllegalArgumentException("Category with name '" + categoryDetails.getName() + "' already exists");
            }

            category.setName(categoryDetails.getName());
            category.setDescription(categoryDetails.getDescription());
            return categoryRepository.save(category);
        }
        throw new IllegalArgumentException("Category not found with id: " + id);
    }

    public void deleteCategory(Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            // Check if category has stories
            if (!category.get().getStories().isEmpty()) {
                throw new IllegalStateException("Cannot delete category with existing stories");
            }
            categoryRepository.deleteById(id);
        } else {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }
    }

    public List<Object[]> getPopularCategories(Pageable pageable) {
        return categoryRepository.findPopularCategories(pageable);
    }

    public List<Category> searchCategories(String query) {
        return categoryRepository.findByNameContainingIgnoreCase(query);
    }

    public Long getStoryCountByCategory(Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        return category.map(c -> (long) c.getStories().size()).orElse(0L);
    }
}
