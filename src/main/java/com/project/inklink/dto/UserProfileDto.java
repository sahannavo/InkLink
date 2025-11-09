package com.project.inklink.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.inklink.entity.enums.UserRole;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private UserRole role;
    private String profilePicture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Statistics
    private Long storyCount;
    private Long publishedStoryCount;
    private Long commentCount;
    private Long totalReads;

    // Constructors
    public UserProfileDto() {}

    public UserProfileDto(Long id, String username, String email, UserRole role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getStoryCount() { return storyCount; }
    public void setStoryCount(Long storyCount) { this.storyCount = storyCount; }

    public Long getPublishedStoryCount() { return publishedStoryCount; }
    public void setPublishedStoryCount(Long publishedStoryCount) { this.publishedStoryCount = publishedStoryCount; }

    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }

    public Long getTotalReads() { return totalReads; }
    public void setTotalReads(Long totalReads) { this.totalReads = totalReads; }

    // Builder pattern
    public static class Builder {
        private UserProfileDto dto;

        public Builder() {
            dto = new UserProfileDto();
        }

        public Builder id(Long id) {
            dto.id = id;
            return this;
        }

        public Builder username(String username) {
            dto.username = username;
            return this;
        }

        public Builder email(String email) {
            dto.email = email;
            return this;
        }

        public Builder role(UserRole role) {
            dto.role = role;
            return this;
        }

        public Builder profilePicture(String profilePicture) {
            dto.profilePicture = profilePicture;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            dto.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            dto.updatedAt = updatedAt;
            return this;
        }

        public Builder storyCount(Long storyCount) {
            dto.storyCount = storyCount;
            return this;
        }

        public Builder publishedStoryCount(Long publishedStoryCount) {
            dto.publishedStoryCount = publishedStoryCount;
            return this;
        }

        public Builder commentCount(Long commentCount) {
            dto.commentCount = commentCount;
            return this;
        }

        public Builder totalReads(Long totalReads) {
            dto.totalReads = totalReads;
            return this;
        }

        public UserProfileDto build() {
            return dto;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "UserProfileDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", storyCount=" + storyCount +
                '}';
    }
}