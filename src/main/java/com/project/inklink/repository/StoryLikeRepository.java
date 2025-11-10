package com.project.inklink.repository;

import com.project.inklink.entity.StoryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryLikeRepository extends JpaRepository<StoryLike, Long> {

    // Check if user has liked a story
    Optional<StoryLike> findByStoryIdAndUserId(Long storyId, Long userId);

    // Count likes for a story
    Long countByStoryId(Long storyId);

    // Delete like by story and user
    void deleteByStoryIdAndUserId(Long storyId, Long userId);

    // Delete all likes for a story
    void deleteByStoryId(Long storyId);

    // Check if like exists
    Boolean existsByStoryIdAndUserId(Long storyId, Long userId);
}