package com.project.inklink.entity;

import com.project.inklink.enums.ReactionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reactions", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"story_id", "user_id"})
})
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReactionType type; // LIKE, LOVE, etc.

    private LocalDateTime createdAt;

    @ManyToOne
    private Story story;

    @ManyToOne
    private User user;

    public Reaction() {
        this.createdAt = LocalDateTime.now();
    }

    public Reaction(ReactionType type, Story story, User user) {
        this();
        this.type = type;
        this.story = story;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ReactionType getType() { return type; }
    public void setType(ReactionType type) { this.type = type; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Story getStory() { return story; }
    public void setStory(Story story) { this.story = story; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
