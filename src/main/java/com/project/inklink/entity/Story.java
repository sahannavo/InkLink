package com.project.inklink.entity;

import com.project.inklink.enums.StoryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

    @Entity
    @Table(name = "stories")
    public class Story {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must be less than 200 characters")
        @Column(nullable = false)
        private String title;

        @NotBlank(message = "Content is required")
        @Size(min = 100, message = "Story must be at least 100 characters")
        @Column(columnDefinition = "TEXT", nullable = false)
        private String content;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "author_id", nullable = false)
        @NotNull(message = "Author is required")
        private User author;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "category_id")
        private Category category;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private StoryStatus status = StoryStatus.DRAFT;

        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "published_at")
        private LocalDateTime publishedAt;

        @Column(name = "reading_time") // in minutes
        private Integer readingTime;

        @Column(name = "view_count")
        private Integer viewCount = 0;

        // Constructors
        public Story() {}

        public Story(String title, String content, User author, Category category) {
            this.title = title;
            this.content = content;
            this.author = author;
            this.category = category;
        }

        @PrePersist
        protected void onCreate() {
            createdAt = LocalDateTime.now();
            calculateReadingTime();
        }

        @PreUpdate
        protected void onUpdate() {
            calculateReadingTime();
            if (status == StoryStatus.PUBLISHED && publishedAt == null) {
                publishedAt = LocalDateTime.now();
            }
        }

        private void calculateReadingTime() {
            // Estimate reading time: 200 words per minute
            int wordCount = content.split("\\s+").length;
            this.readingTime = (int) Math.ceil(wordCount / 200.0);
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getContent() { return content; }
        public void setContent(String content) {
            this.content = content;
            calculateReadingTime();
        }

        public User getAuthor() { return author; }
        public void setAuthor(User author) { this.author = author; }

        public Category getCategory() { return category; }
        public void setCategory(Category category) { this.category = category; }

        public StoryStatus getStatus() { return status; }
        public void setStatus(StoryStatus status) {
            this.status = status;
            if (status == StoryStatus.PUBLISHED && publishedAt == null) {
                publishedAt = LocalDateTime.now();
            }
        }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getPublishedAt() { return publishedAt; }
        public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }

        public Integer getReadingTime() { return readingTime; }
        public void setReadingTime(Integer readingTime) { this.readingTime = readingTime; }

        public Integer getViewCount() { return viewCount; }
        public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
    }

