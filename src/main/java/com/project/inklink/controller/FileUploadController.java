package com.project.inklink.controller;

import com.project.inklink.entity.User;
import com.project.inklink.service.FileStorageService;
import com.project.inklink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/files")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            // Get current user
            String email = authentication.getName();
            Optional<User> userOptional = userService.findByEmail(email);

            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOptional.get();

            // Store the file
            String fileName = fileStorageService.storeFile(file);

            // Delete old avatar if exists
            if (user.getProfilePicture() != null) {
                fileStorageService.deleteFile(user.getProfilePicture());
            }

            // Update user profile picture
            user.setProfilePicture(fileName);
            userService.saveUser(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Avatar uploaded successfully");
            response.put("fileName", fileName);
            response.put("fileUrl", "/api/files/avatar/" + fileName);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/avatar/{fileName:.+}")
    @ResponseBody
    public ResponseEntity<?> serveAvatar(@PathVariable String fileName) {
        try {
            Path file = fileStorageService.loadFile(fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                String contentType = determineContentType(fileName);
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String determineContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}