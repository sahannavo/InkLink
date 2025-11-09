package com.project.inklink.controller;

import com.project.inklink.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    @GetMapping("/popular")
    public ResponseEntity<?> getPopularTags(
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<Map<String, Object>> popularTags = tagService.getPopularTags(limit);
            return ResponseEntity.ok(popularTags);
        } catch (Exception e) {
            e.printStackTrace(); // Add logging
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "error", "Failed to fetch popular tags: " + e.getMessage()
                    )
            );
        }
    }
}