package com.project.inklink.controller;

import com.project.inklink.dto.ApiResponse;
import com.project.inklink.entity.User;
import com.project.inklink.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class FileUploadController {

    @Autowired
    private UserService userService;

    // Define upload directory
    private final String UPLOAD_DIR = "uploads/profiles/";

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        try {
            User user = getCurrentUser(request);
            if (user == null) {
                return ResponseEntity.status(401)
                        .body(new ApiResponse(false, "Authentication required"));
            }

            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "File is empty"));
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Only image files are allowed"));
            }

            // Validate file size (2MB max)
            if (file.getSize() > 2 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "File size must be less than 2MB"));
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null ?
                    originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + fileExtension;

            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Update user profile picture
            String previousPicture = user.getProfilePicture();
            user.setProfilePicture(filename);
            User updatedUser = userService.updateUser(user);

            // Delete previous profile picture if exists
            if (previousPicture != null && !previousPicture.isEmpty()) {
                Path previousFilePath = uploadPath.resolve(previousPicture);
                if (Files.exists(previousFilePath)) {
                    Files.delete(previousFilePath);
                }
            }

            // Update session
            request.getSession().setAttribute("user", updatedUser);

            return ResponseEntity.ok(new ApiResponse(true, "Profile picture uploaded successfully", updatedUser));

        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Failed to upload file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Upload failed: " + e.getMessage()));
        }
    }

    private User getCurrentUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("user");
    }
}
