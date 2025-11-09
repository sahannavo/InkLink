package com.project.inklink.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponse {
    private Long id;
    private String content;
    private UserProfileDto user;
    private Long storyId;
    private LocalDateTime createdAt;
    private Boolean canDelete;

    // Constructors
    public CommentResponse() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public UserProfileDto getUser() { return user; }
    public void setUser(UserProfileDto user) { this.user = user; }

    public Long getStoryId() { return storyId; }
    public void setStoryId(Long storyId) { this.storyId = storyId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getCanDelete() { return canDelete; }
    public void setCanDelete(Boolean canDelete) { this.canDelete = canDelete; }

    // Builder pattern
    public static class Builder {
        private CommentResponse response;

        public Builder() {
            response = new CommentResponse();
        }

        public Builder id(Long id) {
            response.id = id;
            return this;
        }

        public Builder content(String content) {
            response.content = content;
            return this;
        }

        public Builder user(UserProfileDto user) {
            response.user = user;
            return this;
        }

        public Builder storyId(Long storyId) {
            response.storyId = storyId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            response.createdAt = createdAt;
            return this;
        }

        public Builder canDelete(Boolean canDelete) {
            response.canDelete = canDelete;
            return this;
        }

        public CommentResponse build() {
            return response;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}