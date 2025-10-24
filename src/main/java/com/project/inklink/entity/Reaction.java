package com.project.inklink.entity;

import com.project.inklink.enums.ReactionType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
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
}
