package com.project.inklink.service;


import com.project.inklink.config.FileStorageProperties;
import com.project.inklink.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadRoot;
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Autowired
    public FileStorageService(FileStorageProperties props) {
        this.uploadRoot = Paths.get(props.getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadRoot);
        } catch (IOException ex) {
            throw new FileStorageException("Could not create upload directory", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds 2MB limit");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename());
        int idx = original.lastIndexOf('.');
        if (idx <= 0) {
            throw new FileStorageException("File must have an extension");
        }
        String ext = original.substring(idx + 1).toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new FileStorageException("Invalid file type. Allowed: " + ALLOWED_EXT);
        }

        String filename = UUID.randomUUID().toString() + "." + ext;
        Path target = this.uploadRoot.resolve(filename);

        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file", e);
        }
    }

    public Path resolveFile(String filename) {
        Path file = uploadRoot.resolve(filename).normalize();
        if (!Files.exists(file) || !file.startsWith(uploadRoot)) {
            throw new FileStorageException("File not found: " + filename);
        }
        return file;
    }
}