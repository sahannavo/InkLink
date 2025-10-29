package com.project.inklink.service;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.repository.StoryRepository;
import com.project.inklink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoryRepository storyRepository;

    @Value("${app.story.min-content-length:100}")
    private int minContentLength;

    @Value("${app.story.max-content-length:10000}")
    private int maxContentLength;

    @Value("${app.story.max-title-length:200}")
    private int maxTitleLength;

    @Value("${app.user.min-password-length:6}")
    private int minPasswordLength;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    public void validateUserRegistration(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + user.getEmail());
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + user.getUsername());
        }
        if (user.getPassword().length() < minPasswordLength) {
            throw new IllegalArgumentException("Password must be at least " + minPasswordLength + " characters long");
        }
        if (!isValidEmail(user.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!isValidUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username must be 3-50 characters and can only contain letters, numbers, and underscores");
        }
    }

    public void validateStoryCreation(Story story) {
        if (story.getTitle() == null || story.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Story title is required");
        }
        if (story.getTitle().length() > maxTitleLength) {
            throw new IllegalArgumentException("Story title must be less than " + maxTitleLength + " characters");
        }
        if (story.getContent() == null || story.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Story content is required");
        }
        if (story.getContent().length() < minContentLength) {
            throw new IllegalArgumentException("Story must be at least " + minContentLength + " characters long");
        }
        if (story.getContent().length() > maxContentLength) {
            throw new IllegalArgumentException("Story content must be less than " + maxContentLength + " characters");
        }
        if (story.getAuthor() == null) {
            throw new IllegalArgumentException("Story author is required");
        }
        if (story.getAuthor().getId() == null) {
            throw new IllegalArgumentException("Author ID is required");
        }
    }

    public void validateStoryUpdate(Long storyId, Long userId) {
        if (storyId == null) {
            throw new IllegalArgumentException("Story ID is required");
        }
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found with ID: " + storyId));

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
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query cannot be empty");
        }
        if (query.length() > 100) {
            throw new IllegalArgumentException("Search query too long");
        }
        if (query.matches(".[<>\"'].")) {
            throw new IllegalArgumentException("Search query contains invalid characters");
        }
    }

    public void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        if (endDate != null && endDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("End date cannot be in the future");
        }
        if (startDate != null && startDate.isBefore(LocalDateTime.now().minusYears(1))) {
            throw new IllegalArgumentException("Start date cannot be more than 1 year in the past");
        }
    }

    public void validateUserProfileUpdate(User currentUser, User updatedUser) {
        if (currentUser == null || updatedUser == null) {
            throw new IllegalArgumentException("User objects cannot be null");
        }

        if (!currentUser.getEmail().equals(updatedUser.getEmail()) &&
                userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + updatedUser.getEmail());
        }
        if (!currentUser.getUsername().equals(updatedUser.getUsername()) &&
                userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + updatedUser.getUsername());
        }
        if (updatedUser.getBio() != null && updatedUser.getBio().length() > 500) {
            throw new IllegalArgumentException("Bio must be less than 500 characters");
        }
        if (updatedUser.getUsername() != null && !isValidUsername(updatedUser.getUsername())) {
            throw new IllegalArgumentException("Username must be 3-50 characters and can only contain letters, numbers, and underscores");
        }
        if (updatedUser.getEmail() != null && !isValidEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public void validateFileUpload(String originalFilename, long size) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        if (size <= 0) {
            throw new IllegalArgumentException("File size cannot be zero or negative");
        }

        if (size > 2 * 1024 * 1024) { // 2MB
            throw new IllegalArgumentException("File size exceeds 2MB limit");
        }

        // Extract and validate file extension
        int lastDotIndex = originalFilename.lastIndexOf(".");
        if (lastDotIndex == -1) {
            throw new IllegalArgumentException("File must have an extension");
        }
        if (lastDotIndex == originalFilename.length() - 1) {
            throw new IllegalArgumentException("File extension cannot be empty");
        }

        String extension = originalFilename.substring(lastDotIndex + 1).toLowerCase();
        if (!List.of("jpg", "jpeg", "png", "gif", "webp").contains(extension)) {
            throw new IllegalArgumentException("Only image files (JPEG, PNG, GIF, WEBP) are allowed");
        }
    }

    public void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        if (content.length() > 1000) {
            throw new IllegalArgumentException("Comment must be less than 1000 characters");
        }

        if (content.trim().length() < 1) {
            throw new IllegalArgumentException("Comment must contain at least 1 character");
        }

        // Check for excessive whitespace
        if (content.trim().length() < content.length() / 2) {
            throw new IllegalArgumentException("Comment contains excessive whitespace");
        }
    }

    public void validateNotificationAccess(Long notificationId, Long userId) {
        if (notificationId == null || userId == null) {
            throw new IllegalArgumentException("Notification ID and User ID are required");
        }

        // Additional validation logic can be added here
    }

    // Additional validation methods that might be useful

    public void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }

    public void validateStoryId(Long storyId) {
        if (storyId == null) {
            throw new IllegalArgumentException("Story ID is required");
        }
        if (storyId <= 0) {
            throw new IllegalArgumentException("Invalid story ID");
        }
    }

    public void validateContentForProfanity(String content) {
        // Basic profanity filter - extend this list as needed
        List<String> profanityWords = List.of("badword1", "badword2", "offensive");

        String lowerContent = content.toLowerCase();
        for (String word : profanityWords) {
            if (lowerContent.contains(word)) {
                throw new IllegalArgumentException("Content contains inappropriate language");
            }
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }
}