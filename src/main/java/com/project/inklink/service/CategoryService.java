package com.project.inklink.service;




import com.project.inklink.entity.Category;
import com.project.inklink.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

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
        return categoryRepository.findAll();
    }

    public Optional<Category> findByName(String name) {
        return categoryRepository.findByName(name);
    }
}