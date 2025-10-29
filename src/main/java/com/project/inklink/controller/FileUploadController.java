package com.project.inklink.controller;

import com.project.inklink.exception.FileStorageException;
import com.project.inklink.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletContext;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileStorageService storageService;
    private final ServletContext servletContext;

    @Autowired
    public FileUploadController(FileStorageService storageService, ServletContext servletContext) {
        this.storageService = storageService;
        this.servletContext = servletContext;
    }

    /**
     * Uploads an avatar image. Returns JSON with filename and access URL.
     * Authentication is recommended; here Principal is optional.
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAvatar(@RequestPart("file") MultipartFile file,
                                          @AuthenticationPrincipal Principal principal) {
        try {
            String filename = storageService.storeFile(file);
            String base = "/api/files/avatar/";
            String url = base + filename;
            // Optionally associate filename with the authenticated user here
            return ResponseEntity.ok(Map.of("fileName", filename, "url", url));
        } catch (FileStorageException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * Serve avatar by filename. Simple streaming endpoint.
     */
    @GetMapping("/avatar/{filename:.+}")
    public ResponseEntity<Resource> serveAvatar(@PathVariable String filename) {
        try {
            Path filePath = storageService.resolveFile(filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = servletContext.getMimeType(resource.getFile().getAbsolutePath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(CacheControl.noCache())
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (FileStorageException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}


