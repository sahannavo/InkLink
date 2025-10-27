package com.project.inklink.controller;

import com.project.inklink.service.EmailService;
import com.project.inklink.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TestController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/test/upload")
    public String showUploadTest() {
        return "test-upload";
    }

    @PostMapping("/test/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        try {
            String fileName = fileStorageService.storeFile(file);
            redirectAttributes.addFlashAttribute("message",
                    "File uploaded successfully: " + fileName);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message",
                    "Failed to upload file: " + e.getMessage());
        }
        return "redirect:/test/upload";
    }

    @GetMapping("/test/email")
    public String testEmail() {
        try {
            emailService.sendWelcomeEmail("test@example.com", "Test User");
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Failed to send email: " + e.getMessage();
        }
    }
}