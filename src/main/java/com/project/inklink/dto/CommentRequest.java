package com.project.inklink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentRequest {

    @NotBlank(message = "Comment content is required")
    @Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
    private String content;

    // Constructors
    public CommentRequest() {}

    public CommentRequest(String content) {
        this.content = content;
    }

    // Getters and Setters
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @Override
    public String toString() {
        return "CommentRequest{" +
                "content='" + content + '\'' +
                '}';
    }
}