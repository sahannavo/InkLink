package com.project.inklink.service;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.StoryLike;
import com.project.inklink.entity.User;
import com.project.inklink.entity.enums.StoryGenre;
import com.project.inklink.entity.enums.StoryStatus;
import com.project.inklink.repository.StoryLikeRepository;
import com.project.inklink.repository.StoryRepository;
import com.project.inklink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private StoryLikeRepository storyLikeRepository;

    @Autowired
    private UserRepository userRepository;

    // Get all published stories with pagination
    public Page<Story> getPublishedStories(Pageable pageable) {
        return storyRepository.findByStatus(StoryStatus.PUBLISHED, pageable);
    }

    // Search stories
    public Page<Story> searchStories(String searchTerm, Pageable pageable) {
        return storyRepository.searchStories(searchTerm, pageable);
    }

    // Get stories by genre
    public Page<Story> getStoriesByGenre(StoryGenre genre, Pageable pageable) {
        return storyRepository.findByGenreAndStatus(genre, StoryStatus.PUBLISHED, pageable);
    }

    // Get story by ID
    public Optional<Story> getStoryById(Long id) {
        return storyRepository.findById(id);
    }

    // Create story
    public Story createStory(Story story) {
        return storyRepository.save(story);
    }

    // Update story
    public Story updateStory(Story story) {
        return storyRepository.save(story);
    }

    // Delete story
    public void deleteStory(Long id) {
        storyRepository.deleteById(id);
    }

    // Check if user is story author
    public boolean isStoryAuthor(Long storyId, Long userId) {
        Optional<Story> story = storyRepository.findById(storyId);
        return story.isPresent() &&
                story.get().getAuthor() != null &&
                story.get().getAuthor().getId().equals(userId);
    }

    // Get user's stories
    public List<Story> getUserStories(User author) {
        return storyRepository.findByAuthor(author);
    }

    // Get user's published stories
    public List<Story> getUserPublishedStories(User author) {
        return storyRepository.findByAuthorAndStatus(author, StoryStatus.PUBLISHED);
    }

    // Get user's draft stories
    public List<Story> getUserDraftStories(User author) {
        return storyRepository.findByAuthorAndStatus(author, StoryStatus.DRAFT);
    }

    /**
     * Toggle like/unlike for a story
     */
    public boolean toggleLike(Long storyId, Long userId) {
        // Check if story exists
        Optional<Story> storyOpt = storyRepository.findById(storyId);
        if (storyOpt.isEmpty()) {
            throw new RuntimeException("Story not found with id: " + storyId);
        }

        // Check if user exists
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with id: " + userId);
        }

        Story story = storyOpt.get();
        User user = userOpt.get();

        // Check if user already liked the story
        Optional<StoryLike> existingLike = storyLikeRepository.findByStoryIdAndUserId(storyId, userId);

        if (existingLike.isPresent()) {
            // Unlike: remove the like
            storyLikeRepository.delete(existingLike.get());

            // Update story like count
            Long currentLikeCount = storyLikeRepository.countByStoryId(storyId);
            story.setLikeCount(currentLikeCount.intValue());
            storyRepository.save(story);

            return false; // Unliked
        } else {
            // Like: create new like
            StoryLike newLike = new StoryLike(story, user);
            storyLikeRepository.save(newLike);

            // Update story like count
            Long currentLikeCount = storyLikeRepository.countByStoryId(storyId);
            story.setLikeCount(currentLikeCount.intValue());
            storyRepository.save(story);

            return true; // Liked
        }
    }

    /**
     * Check if user has liked a story
     */
    public boolean hasUserLikedStory(Long storyId, Long userId) {
        return storyLikeRepository.existsByStoryIdAndUserId(storyId, userId);
    }

    /**
     * Get like count for a story
     */
    public int getLikeCount(Long storyId) {
        Long count = storyLikeRepository.countByStoryId(storyId);
        return count != null ? count.intValue() : 0;
    }

    /**
     * Increment read count for a story
     */
    public void incrementReadCount(Long storyId) {
        Optional<Story> storyOpt = storyRepository.findById(storyId);
        if (storyOpt.isPresent()) {
            Story story = storyOpt.get();
            int currentReadCount = story.getReadCount() != null ? story.getReadCount() : 0;
            story.setReadCount(currentReadCount + 1);
            storyRepository.save(story);
        } else {
            throw new RuntimeException("Story not found with id: " + storyId);
        }
    }

    /**
     * Get read count for a story
     */
    public int getReadCount(Long storyId) {
        Optional<Story> storyOpt = storyRepository.findById(storyId);
        if (storyOpt.isPresent()) {
            Integer readCount = storyOpt.get().getReadCount();
            return readCount != null ? readCount : 0;
        }
        return 0;
    }

    /**
     * Get story with like information for a specific user
     */
    public Story getStoryWithLikeInfo(Long storyId, Long userId) {
        Optional<Story> storyOpt = storyRepository.findById(storyId);
        if (storyOpt.isPresent()) {
            Story story = storyOpt.get();

            // Set like count
            Long likeCount = storyLikeRepository.countByStoryId(storyId);
            story.setLikeCount(likeCount != null ? likeCount.intValue() : 0);

            return story;
        }
        throw new RuntimeException("Story not found with id: " + storyId);
    }
}