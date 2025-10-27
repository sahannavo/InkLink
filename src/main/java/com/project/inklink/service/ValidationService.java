package com.project.inklink.service;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.StoryStatus;
import com.project.inklink.repository.StoryRepository;
import com.project.inklink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ValidationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    public void validateUserRegistration(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + user.getUsername());
        }
        if (user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    public void validateStoryCreation(Story story) {
        if (story.getTitle() == null || story.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Story title is required");
        }
        if (story.getTitle().length() > 200) {
            throw new IllegalArgumentException("Story title must be less than 200 characters");
        }
        if (story.getContent() == null || story.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Story content is required");
        }
        if (story.getContent().length() < 100) {
            throw new IllegalArgumentException("Story must be at least 100 characters long");
        }
        if (story.getAuthor() == null) {
            throw new IllegalArgumentException("Story author is required");
        }
    }

    public void validateStoryUpdate(Long storyId, Long userId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));

        if (!story.getAuthor().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own stories");
        }
    }

    public void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
    }

    public void validateSearchQuery(String query) {
        if (query != null && query.length() > 100) {
            throw new IllegalArgumentException("Search query too long");
        }
    }

    public void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (endDate != null && endDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }
    }
}