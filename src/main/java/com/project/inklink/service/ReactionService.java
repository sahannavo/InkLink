package com.project.inklink.service;

import com.project.inklink.entity.Reaction;
import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.ReactionType;
import com.project.inklink.repository.ReactionRepository;
import com.project.inklink.repository.StoryRepository;
import com.project.inklink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ReactionService {

    private final ReactionRepository reactionRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Autowired
    public ReactionService(ReactionRepository reactionRepository,
                           StoryRepository storyRepository,
                           UserRepository userRepository,
                           NotificationService notificationService) {
        this.reactionRepository = reactionRepository;
        this.storyRepository = storyRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public Map<String, Object> toggleLike(Long storyId, Long userId) {
        return toggleReaction(storyId, userId, ReactionType.LIKE);
    }

    public Map<String, Object> toggleBookmark(Long storyId, Long userId) {
        return toggleReaction(storyId, userId, ReactionType.BOOKMARK);
    }

    private Map<String, Object> toggleReaction(Long storyId, Long userId, ReactionType type) {
        Map<String, Object> response = new HashMap<>();

        Optional<Story> storyOpt = storyRepository.findById(storyId);
        Optional<User> userOpt = userRepository.findById(userId);

        if (storyOpt.isEmpty() || userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Story or user not found");
            return response;
        }

        Story story = storyOpt.get();
        User user = userOpt.get();

        Optional<Reaction> existingReaction = reactionRepository.findByUserAndStoryAndType(user, story, type);

        if (existingReaction.isPresent()) {
            // Remove reaction
            reactionRepository.delete(existingReaction.get());
            response.put("action", "removed");
            response.put("message", type + " removed successfully");
        } else {
            // Add reaction
            Reaction reaction = new Reaction(type, story, user);
            reactionRepository.save(reaction);
            response.put("action", "added");
            response.put("message", type + " added successfully");

            // Send notification for likes
            if (type == ReactionType.LIKE) {
                notificationService.createStoryLikeNotification(story, user);
            }
        }

        // Get updated count
        Long count = reactionRepository.countByStoryAndType(story, type);
        response.put("success", true);
        response.put("count", count);
        response.put("isActive", existingReaction.isEmpty());

        return response;
    }

    public Long getLikeCount(Long storyId) {
        Optional<Story> story = storyRepository.findById(storyId);
        return story.map(s -> reactionRepository.countByStoryAndType(s, ReactionType.LIKE))
                .orElse(0L);
    }

    public Long getBookmarkCount(Long storyId) {
        Optional<Story> story = storyRepository.findById(storyId);
        return story.map(s -> reactionRepository.countByStoryAndType(s, ReactionType.BOOKMARK))
                .orElse(0L);
    }

    public Map<String, Boolean> getUserReactions(Long userId, List<Long> storyIds) {
        Map<String, Boolean> reactions = new HashMap<>();

        List<Long> likedStories = reactionRepository.findUserReactionsForStories(userId, ReactionType.LIKE, storyIds);
        List<Long> bookmarkedStories = reactionRepository.findUserReactionsForStories(userId, ReactionType.BOOKMARK, storyIds);

        for (Long storyId : storyIds) {
            reactions.put("story_" + storyId + "_liked", likedStories.contains(storyId));
            reactions.put("story_" + storyId + "_bookmarked", bookmarkedStories.contains(storyId));
        }

        return reactions;
    }

    public Page<Story> getUserBookmarks(Long userId, Pageable pageable) {
        return reactionRepository.findBookmarksByUser(userId, pageable);
    }

    public Page<Story> getUserLikedStories(Long userId, Pageable pageable) {
        return reactionRepository.findLikedStoriesByUser(userId, pageable);
    }

    public boolean hasUserLikedStory(Long userId, Long storyId) {
        return reactionRepository.existsByUserAndStoryAndType(userId, storyId, ReactionType.LIKE);
    }

    public boolean hasUserBookmarkedStory(Long userId, Long storyId) {
        return reactionRepository.existsByUserAndStoryAndType(userId, storyId, ReactionType.BOOKMARK);
    }
}