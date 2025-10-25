package com.project.inklink.repository;

import com.project.inklink.entity.Reaction;
import com.project.inklink.entity.Story;
import com.project.inklink.enums.ReactionType;
import org.apache.catalina.User;
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

    // Count likes/bookmarks for a story (for like counts display)
    Long countByStoryAndType(Story story, ReactionType type);

    // Check if user already reacted to a story (for toggle functionality)
    Optional<Reaction> findByUserAndStoryAndType(User user, Story story, ReactionType type);

    // Get user's bookmarked stories for profile page
    @Query("SELECT r.story FROM Reaction r WHERE r.user.id = :userId AND r.type = 'BOOKMARK'")
    Page<Story> findBookmarksByUser(@Param("userId") Long userId, Pageable pageable);

    // Delete reaction (for toggle functionality)
    void deleteByUserAndStoryAndType(User user, Story story, ReactionType type);

    // Get all reactions by user for profile
    List<Reaction> findByUserAndType(User user, ReactionType type);

    // Get like count for multiple stories (performance optimization)
    @Query("SELECT r.story.id, COUNT(r) FROM Reaction r WHERE r.story.id IN :storyIds AND r.type = 'LIKE' GROUP BY r.story.id")
    List<Object[]> countLikesByStories(@Param("storyIds") List<Long> storyIds);

    // Check which stories user has liked/bookmarked
    @Query("SELECT r.story.id FROM Reaction r WHERE r.user.id = :userId AND r.type = :type AND r.story.id IN :storyIds")
    List<Long> findUserReactionsForStories(@Param("userId") Long userId,
                                           @Param("type") ReactionType type,
                                           @Param("storyIds") List<Long> storyIds);
}
