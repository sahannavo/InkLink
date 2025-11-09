package com.project.inklink.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FileUploadUtil {

    // Configuration constants
    public static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    public static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );
    public static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/plain"
    );

    // Directory constants
    public static final String PROFILE_UPLOAD_DIR = "uploads/profiles/";
    public static final String STORY_UPLOAD_DIR = "uploads/stories/";
    public static final String TEMP_UPLOAD_DIR = "uploads/temp/";

    /**
     * Validate file for upload
     */
    public static void validateFile(MultipartFile file, boolean isImage) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("File size must be less than " + (MAX_FILE_SIZE / 1024 / 1024) + "MB");
        }

        if (isImage) {
            validateImageFile(file);
        }
    }

    /**
     * Validate image file specifically
     */
    public static void validateImageFile(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IOException(
                    "Only image files are allowed. Supported types: " +
                            String.join(", ", ALLOWED_IMAGE_TYPES)
            );
        }
    }

    /**
     * Generate unique filename
     */
    public static String generateUniqueFilename(String originalFilename) {
        String fileExtension = getFileExtension(originalFilename);
        return UUID.randomUUID().toString() + fileExtension;
    }

    /**
     * Get file extension from filename
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    /**
     * Save file to specified directory
     */
    public static String saveFile(MultipartFile file, String uploadDir) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String filename = generateUniqueFilename(file.getOriginalFilename());

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    /**
     * Save profile picture
     */
    public static String saveProfilePicture(MultipartFile file) throws IOException {
        validateImageFile(file);
        return saveFile(file, PROFILE_UPLOAD_DIR);
    }

    /**
     * Save story image
     */
    public static String saveStoryImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        return saveFile(file, STORY_UPLOAD_DIR);
    }

    /**
     * Delete file from directory
     */
    public static boolean deleteFile(String filename, String uploadDir) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Delete profile picture
     */
    public static boolean deleteProfilePicture(String filename) {
        return deleteFile(filename, PROFILE_UPLOAD_DIR);
    }

    /**
     * Delete story image
     */
    public static boolean deleteStoryImage(String filename) {
        return deleteFile(filename, STORY_UPLOAD_DIR);
    }

    /**
     * Get file size in human readable format
     */
    public static String getReadableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    /**
     * Check if file is an image
     */
    public static boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Get MIME type from filename
     */
    public static String getMimeType(String filename) {
        try {
            Path path = Paths.get(filename);
            return Files.probeContentType(path);
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    /**
     * Clean up temporary files
     */
    public static void cleanupTempFiles() {
        try {
            Path tempPath = Paths.get(TEMP_UPLOAD_DIR);
            if (Files.exists(tempPath)) {
                Files.walk(tempPath)
                        .filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                // Log warning but continue
                                System.err.println("Failed to delete temp file: " + path);
                            }
                        });
            }
        } catch (IOException e) {
            // Log warning but don't throw
            System.err.println("Failed to cleanup temp files: " + e.getMessage());
        }
    }

    /**
     * Get file URL for frontend
     */
    public static String getFileUrl(String filename, String fileType) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }

        switch (fileType.toLowerCase()) {
            case "profile":
                return "/api/files/profiles/" + filename;
            case "story":
                return "/api/files/stories/" + filename;
            default:
                return "/api/files/" + filename;
        }
    }

    /**
     * Get profile picture URL
     */
    public static String getProfilePictureUrl(String filename) {
        return getFileUrl(filename, "profile");
    }

    /**
     * Get story image URL
     */
    public static String getStoryImageUrl(String filename) {
        return getFileUrl(filename, "story");
    }
}