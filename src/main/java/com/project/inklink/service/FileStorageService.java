package com.project.inklink.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    // Define upload directories
    private final String PROFILE_UPLOAD_DIR = "uploads/profiles/";
    private final String STORY_UPLOAD_DIR = "uploads/stories/";

    // Allowed file types
    private final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    private final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    public String storeProfilePicture(MultipartFile file) throws IOException {
        return storeFile(file, PROFILE_UPLOAD_DIR, true);
    }

    public String storeStoryImage(MultipartFile file) throws IOException {
        return storeFile(file, STORY_UPLOAD_DIR, true);
    }

    public String storeFile(MultipartFile file, String uploadDir, boolean isImage) throws IOException {
        // Validate file
        validateFile(file, isImage);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + fileExtension;

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public void deleteFile(String filename, String uploadDir) throws IOException {
        if (filename != null && !filename.isEmpty()) {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
    }

    public void deleteProfilePicture(String filename) throws IOException {
        deleteFile(filename, PROFILE_UPLOAD_DIR);
    }

    public void deleteStoryImage(String filename) throws IOException {
        deleteFile(filename, STORY_UPLOAD_DIR);
    }

    // Validation methods
    private void validateFile(MultipartFile file, boolean isImage) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size must be less than 2MB");
        }

        if (isImage) {
            validateImageFile(file);
        }
    }

    private void validateImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedImageType(contentType)) {
            throw new RuntimeException("Only image files are allowed (JPEG, PNG, GIF, WebP)");
        }
    }

    private boolean isAllowedImageType(String contentType) {
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg"; // Default extension
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    // Utility methods
    public byte[] getFileBytes(String filename, String uploadDir) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        throw new RuntimeException("File not found: " + filename);
    }

    public byte[] getProfilePictureBytes(String filename) throws IOException {
        return getFileBytes(filename, PROFILE_UPLOAD_DIR);
    }

    public boolean fileExists(String filename, String uploadDir) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        Path filePath = Paths.get(uploadDir).resolve(filename);
        return Files.exists(filePath);
    }

    public String getProfilePictureUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return "/api/files/profiles/" + filename;
    }

    public String getStoryImageUrl(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        return "/api/files/stories/" + filename;
    }
}