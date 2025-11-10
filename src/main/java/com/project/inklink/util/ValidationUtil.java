package com.project.inklink.util;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class ValidationUtil {

    // Validation patterns
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[a-zA-Z0-9_]{3,50}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^.{6,}$"); // At least 6 characters

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z\\s]{2,100}$");

    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$");

    private static final Pattern HTML_TAG_PATTERN =
            Pattern.compile("<[^>]*>");

    /**
     * Validate email address
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validate username
     */
    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    /**
     * Validate password strength
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Validate and sanitize text input (prevent XSS)
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // Remove HTML tags to prevent XSS
        String sanitized = HTML_TAG_PATTERN.matcher(input).replaceAll("");

        // Trim and return
        return sanitized.trim();
    }

    /**
     * Validate story title
     */
    public static boolean isValidStoryTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        String sanitized = sanitizeInput(title);
        return sanitized.length() >= 3 && sanitized.length() <= 255;
    }

    /**
     * Validate story content
     */
    public static boolean isValidStoryContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        String sanitized = sanitizeInput(content);
        return sanitized.length() >= 10;
    }

    /**
     * Validate comment content
     */
    public static boolean isValidCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        String sanitized = sanitizeInput(content);
        return sanitized.length() >= 1 && sanitized.length() <= 1000;
    }

    /**
     * Validate tag name
     */
    public static boolean isValidTagName(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return false;
        }
        String sanitized = sanitizeInput(tagName);
        return sanitized.length() >= 2 && sanitized.length() <= 50;
    }

    /**
     * Validate URL
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url.trim()).matches();
    }

    /**
     * Validate file name
     */
    public static boolean isValidFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        // Check for path traversal attempts
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return false;
        }

        // Check length
        return filename.length() <= 255;
    }

    /**
     * Validate enum value
     */
    public static <T extends Enum<T>> boolean isValidEnum(Class<T> enumClass, String value) {
        if (value == null) {
            return false;
        }
        try {
            Enum.valueOf(enumClass, value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validate numeric range
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        double doubleValue = value.doubleValue();
        return doubleValue >= min.doubleValue() && doubleValue <= max.doubleValue();
    }

    /**
     * Validate positive number
     */
    public static boolean isPositive(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() > 0;
    }

    /**
     * Validate non-negative number
     */
    public static boolean isNonNegative(Number value) {
        if (value == null) {
            return false;
        }
        return value.doubleValue() >= 0;
    }

    /**
     * Validate string length
     */
    public static boolean isValidLength(String value, int min, int max) {
        if (value == null) {
            return false;
        }
        int length = value.trim().length();
        return length >= min && length <= max;
    }

    /**
     * Validate list size
     */
    public static <T> boolean isValidListSize(List<T> list, int min, int max) {
        if (list == null) {
            return false;
        }
        return list.size() >= min && list.size() <= max;
    }

    /**
     * Get validation errors for user registration
     */
    public static Map<String, String> validateUserRegistration(String username, String email, String password) {
        Map<String, String> errors = new HashMap<>();

        if (!isValidUsername(username)) {
            errors.put("username", "Username must be 3-50 characters and contain only letters, numbers, and underscores");
        }

        if (!isValidEmail(email)) {
            errors.put("email", "Please provide a valid email address");
        }

        if (!isValidPassword(password)) {
            errors.put("password", "Password must be at least 6 characters long");
        }

        return errors;
    }

    /**
     * Get validation errors for story creation
     */
    public static Map<String, String> validateStoryCreation(String title, String content) {
        Map<String, String> errors = new HashMap<>();

        if (!isValidStoryTitle(title)) {
            errors.put("title", "Title must be between 3 and 255 characters");
        }

        if (!isValidStoryContent(content)) {
            errors.put("content", "Content must be at least 10 characters long");
        }

        return errors;
    }

    /**
     * Get validation errors for comment creation
     */
    public static Map<String, String> validateCommentCreation(String content) {
        Map<String, String> errors = new HashMap<>();

        if (!isValidCommentContent(content)) {
            errors.put("content", "Comment must be between 1 and 1000 characters");
        }

        return errors;
    }

    /**
     * Check if validation has errors
     */
    public static boolean hasValidationErrors(Map<String, String> errors) {
        return errors != null && !errors.isEmpty();
    }

    /**
     * Sanitize and validate search query
     */
    public static String sanitizeSearchQuery(String query) {
        if (query == null) {
            return "";
        }

        String sanitized = sanitizeInput(query);

        // Limit length for performance
        if (sanitized.length() > 100) {
            sanitized = sanitized.substring(0, 100);
        }

        return sanitized;
    }

    /**
     * Validate file MIME type
     */
    public static boolean isValidMimeType(String mimeType, List<String> allowedTypes) {
        if (mimeType == null || allowedTypes == null) {
            return false;
        }
        return allowedTypes.contains(mimeType.toLowerCase());
    }

    /**
     * Validate file size
     */
    public static boolean isValidFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }
}