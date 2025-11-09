package com.project.inklink.dto;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.inklink.entity.enums.StoryGenre;
import com.project.inklink.entity.enums.StoryStatus;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoryResponse {
    private Long id;
    private String title;
    private String content;
    private StoryGenre genre;
    private StoryStatus status;
    private Integer readCount;
    private UserProfileDto author;
    private List<CommentResponse> comments;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long commentCount;
    private Boolean isAuthor;

    // Constructors
    public StoryResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public StoryGenre getGenre() { return genre; }
    public void setGenre(StoryGenre genre) { this.genre = genre; }

    public StoryStatus getStatus() { return status; }
    public void setStatus(StoryStatus status) { this.status = status; }

    public Integer getReadCount() { return readCount; }
    public void setReadCount(Integer readCount) { this.readCount = readCount; }

    public UserProfileDto getAuthor() { return author; }
    public void setAuthor(UserProfileDto author) { this.author = author; }

    public List<CommentResponse> getComments() { return comments; }
    public void setComments(List<CommentResponse> comments) { this.comments = comments; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }

    public Boolean getIsAuthor() { return isAuthor; }
    public void setIsAuthor(Boolean isAuthor) { this.isAuthor = isAuthor; }

    // Builder pattern for fluent creation
    public static class Builder {
        private StoryResponse response;

        public Builder() {
            response = new StoryResponse();
        }

        public Builder id(Long id) {
            response.id = id;
            return this;
        }

        public Builder title(String title) {
            response.title = title;
            return this;
        }

        public Builder content(String content) {
            response.content = content;
            return this;
        }

        public Builder genre(StoryGenre genre) {
            response.genre = genre;
            return this;
        }

        public Builder status(StoryStatus status) {
            response.status = status;
            return this;
        }

        public Builder readCount(Integer readCount) {
            response.readCount = readCount;
            return this;
        }

        public Builder author(UserProfileDto author) {
            response.author = author;
            return this;
        }

        public Builder comments(List<CommentResponse> comments) {
            response.comments = comments;
            return this;
        }

        public Builder tags(List<String> tags) {
            response.tags = tags;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            response.updatedAt = updatedAt;
            return this;
        }

        public Builder commentCount(Long commentCount) {
            response.commentCount = commentCount;
            return this;
        }

        public Builder isAuthor(Boolean isAuthor) {
            response.isAuthor = isAuthor;
            return this;
        }

        public StoryResponse build() {
            return response;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}