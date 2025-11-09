package com.project.inklink.repository;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.Tag;
import com.project.inklink.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    // Find tag by name
    Optional<Tag> findByName(String name);

    // Find tags by name containing (case insensitive)
    List<Tag> findByNameContainingIgnoreCase(String name);

    // Check if tag exists by name
    Boolean existsByName(String name);

    // Find popular tags (by story count)
    @Query("SELECT t, COUNT(s) as storyCount FROM Tag t LEFT JOIN t.stories s WHERE s.status = 'PUBLISHED' GROUP BY t ORDER BY storyCount DESC")
    Page<Object[]> findPopularTags(Pageable pageable);

    // Find tags by story
    @Query("SELECT t FROM Tag t JOIN t.stories s WHERE s = :story")
    List<Tag> findByStory(@Param("story") Story story);

    // Find tags with story count
    @Query("SELECT t, COUNT(s) as storyCount FROM Tag t LEFT JOIN t.stories s GROUP BY t")
    List<Object[]> findAllWithStoryCount();

    // Find tags used by a specific user
    @Query("SELECT DISTINCT t FROM Tag t JOIN t.stories s WHERE s.author = :user")
    List<Tag> findByUserStories(@Param("user") User user);

    // Find tags with at least minimum number of stories
    @Query("SELECT t FROM Tag t WHERE (SELECT COUNT(s) FROM t.stories s WHERE s.status = 'PUBLISHED') >= :minStories")
    List<Tag> findTagsWithMinimumStories(@Param("minStories") Long minStories);

    // Search tags by name with pagination
    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Find unused tags (tags with no stories)
    @Query("SELECT t FROM Tag t WHERE t.stories IS EMPTY")
    List<Tag> findUnusedTags();

    // Find tags by multiple names
    List<Tag> findByNameIn(List<String> names);

    // Count stories by tag
    @Query("SELECT COUNT(s) FROM Story s JOIN s.tags t WHERE t = :tag AND s.status = 'PUBLISHED'")
    Long countPublishedStoriesByTag(@Param("tag") Tag tag);

    // Find related tags (tags that appear together in stories)
    @Query("SELECT DISTINCT t2 FROM Tag t1 JOIN t1.stories s1 JOIN s1.tags t2 WHERE t1 = :tag AND t2 != :tag")
    List<Tag> findRelatedTags(@Param("tag") Tag tag);
}