package com.project.inklink.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoryRepository extends JpaRepository<com.project.inklink.entity.Story, Long> {
    // Basic methods - we'll add custom queries tomorrow
    Long countByAuthorIdAndStatus(Long authorId, com.project.inklink.enums.StoryStatus status);
}

