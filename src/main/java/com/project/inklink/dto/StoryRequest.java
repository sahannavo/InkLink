package com.project.inklink.dto;

import com.project.inklink.entity.enums.StoryGenre;
import com.project.inklink.entity.enums.StoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class StoryRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    private String content;

    @NotNull(message = "Genre is required")
    private StoryGenre genre;

    private StoryStatus status = StoryStatus.DRAFT;

    // Constructors
    public StoryRequest() {}

    public StoryRequest(String title, String content, StoryGenre genre, StoryStatus status) {
        this.title = title;
        this.content = content;
        this.genre = genre;
        this.status = status;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public StoryGenre getGenre() { return genre; }
    public void setGenre(StoryGenre genre) { this.genre = genre; }

    public StoryStatus getStatus() { return status; }
    public void setStatus(StoryStatus status) { this.status = status; }

    @Override
    public String toString() {
        return "StoryRequest{" +
                "title='" + title + '\'' +
                ", genre=" + genre +
                ", status=" + status +
                '}';
    }
}