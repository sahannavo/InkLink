package com.project.inklink.repository;

import com.project.inklink.entity.Reaction;
import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.ReactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    Long countByStoryAndType(Story story, ReactionType type);

    Optional<Reaction> findByUserAndStoryAndType(User user, Story story, ReactionType type);

    @Query("SELECT r.story FROM Reaction r WHERE r.user.id = :userId AND r.type = 'BOOKMARK'")
    Page<Story> findBookmarksByUser(@Param("userId") Long userId, Pageable pageable);

    void deleteByUserAndStoryAndType(User user, Story story, ReactionType type);

    List<Reaction> findByUserAndType(User user, ReactionType type);

    @Query("SELECT r.story.id, COUNT(r) FROM Reaction r WHERE r.story.id IN :storyIds AND r.type = 'LIKE' GROUP BY r.story.id")
    List<Object[]> countLikesByStories(@Param("storyIds") List<Long> storyIds);

    @Query("SELECT r.story.id FROM Reaction r WHERE r.user.id = :userId AND r.type = :type AND r.story.id IN :storyIds")
    List<Long> findUserReactionsForStories(@Param("userId") Long userId,
                                           @Param("type") ReactionType type,
                                           @Param("storyIds") List<Long> storyIds);

    // Additional methods for analytics
    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.story.id = :storyId AND r.type = 'LIKE'")
    Long countLikesByStoryId(@Param("storyId") Long storyId);

    @Query("SELECT COUNT(r) FROM Reaction r WHERE r.story.id = :storyId AND r.type = 'BOOKMARK'")
    Long countBookmarksByStoryId(@Param("storyId") Long storyId);

    // Check if user has reacted to a story
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Reaction r " +
            "WHERE r.user.id = :userId AND r.story.id = :storyId AND r.type = :type")
    Boolean existsByUserAndStoryAndType(@Param("userId") Long userId,
                                        @Param("storyId") Long storyId,
                                        @Param("type") ReactionType type);

    // Get user's liked stories
    @Query("SELECT r.story FROM Reaction r WHERE r.user.id = :userId AND r.type = 'LIKE'")
    Page<Story> findLikedStoriesByUser(@Param("userId") Long userId, Pageable pageable);
}