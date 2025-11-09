package com.project.inklink.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "story_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"story_id", "user_id"})
})
public class StoryLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime likedAt;

    // Constructors
    public StoryLike() {
        this.likedAt = LocalDateTime.now();
    }

    public StoryLike(Story story, User user) {
        this();
        this.story = story;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Story getStory() { return story; }
    public void setStory(Story story) { this.story = story; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getLikedAt() { return likedAt; }
    public void setLikedAt(LocalDateTime likedAt) { this.likedAt = likedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoryLike)) return false;
        StoryLike storyLike = (StoryLike) o;
        return id != null && id.equals(storyLike.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
