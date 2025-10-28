package com.project.inklink.service;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.StoryStatus;
import com.project.inklink.repository.StoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StoryService {

    private final StoryRepository storyRepository;
    private final ValidationService validationService;
    private final AnalyticsService analyticsService;

    public StoryService(StoryRepository storyRepository,
                        ValidationService validationService,
                        AnalyticsService analyticsService) {
        this.storyRepository = storyRepository;
        this.validationService = validationService;
        this.analyticsService = analyticsService;
    }

    public Story createStory(Story story, User author) {
        validationService.validateStoryCreation(story);

        story.setAuthor(author);
        story.setStatus(StoryStatus.DRAFT);
        story.calculateReadingTime();

        Story savedStory = storyRepository.save(story);

        // Log analytics
        analyticsService.logStoryCreation(savedStory);

        return savedStory;
    }

    public Story updateStory(Long storyId, Story storyDetails, Long userId) {
        validationService.validateStoryUpdate(storyId, userId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));

        // Track changes for audit
        String originalTitle = story.getTitle();

        story.setTitle(storyDetails.getTitle());
        story.setContent(storyDetails.getContent());
        story.setExcerpt(storyDetails.getExcerpt());
        story.setCategory(storyDetails.getCategory());
        story.setCoverImage(storyDetails.getCoverImage());
        story.calculateReadingTime();

        Story updatedStory = storyRepository.save(story);

        // Log update in analytics
        analyticsService.logStoryUpdate(updatedStory, originalTitle);

        return updatedStory;
    }

    public Story publishStory(Long storyId, Long userId) {
        validationService.validateStoryUpdate(storyId, userId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));

        if (!story.canPublish()) {
            throw new IllegalStateException("Story cannot be published. Check content length and status.");
        }

        story.setStatus(StoryStatus.PUBLISHED);
        story.setPublishedAt(LocalDateTime.now());

        Story publishedStory = storyRepository.save(story);

        // Log publication
        analyticsService.logStoryPublication(publishedStory);

        return publishedStory;
    }

    public void deleteStory(Long storyId, Long userId) {
        validationService.validateStoryUpdate(storyId, userId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));

        // Log deletion before actual deletion
        analyticsService.logStoryDeletion(story);

        storyRepository.delete(story);
    }

    @Transactional(readOnly = true)
    public Page<Story> getPublishedStories(Pageable pageable) {
        return storyRepository.findPublishedStoriesWithPagination(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Story> getUserStories(Long userId, StoryStatus status, Pageable pageable) {
        return storyRepository.findByAuthorWithPagination(userId, status, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Story> getStoryById(Long id) {
        Optional<Story> story = storyRepository.findById(id);

        // Log view for analytics (if published)
        story.ifPresent(s -> {
            if (s.getStatus() == StoryStatus.PUBLISHED) {
                analyticsService.logStoryView(s);
            }
        });

        return story;
    }

    public Story incrementViewCount(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));

        story.incrementViewCount();
        Story updatedStory = storyRepository.save(story);

        // Log view in analytics
        analyticsService.logStoryView(updatedStory);

        return updatedStory;
    }

    @Transactional(readOnly = true)
    public List<Story> getTrendingStories(Pageable pageable) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return storyRepository.findTrendingStories(weekAgo, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Story> searchStories(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return getPublishedStories(pageable);
        }

        // Log search query for analytics
        analyticsService.logSearchQuery(query);

        return storyRepository.searchByTitleOrContent(query.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Story> searchByCategory(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty()) {
            return getPublishedStories(pageable);
        }
        return storyRepository.findByCategoryName(category.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public boolean isStoryOwner(Long storyId, Long userId) {
        return storyRepository.findById(storyId)
                .map(story -> story.getAuthor().getId().equals(userId))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public List<Story> getRelatedStories(Long storyId, int limit) {
        Optional<Story> storyOpt = storyRepository.findById(storyId);
        if (storyOpt.isEmpty()) {
            return List.of();
        }

        Story story = storyOpt.get();
        if (story.getCategory() == null) {
            return List.of();
        }

        Pageable pageable = Pageable.ofSize(limit);
        return storyRepository.findRelatedStories(
                story.getCategory().getName(),
                storyId,
                pageable
        ).getContent();
    }

    @Transactional(readOnly = true)
    public Long getUserStoryCount(Long userId, StoryStatus status) {
        return storyRepository.countByAuthorIdAndStatus(userId, status);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getUserStoryStats(Long userId) {
        return List.of(
                new Object[]{"DRAFT", storyRepository.countByAuthorIdAndStatus(userId, StoryStatus.DRAFT)},
                new Object[]{"PUBLISHED", storyRepository.countByAuthorIdAndStatus(userId, StoryStatus.PUBLISHED)},
                new Object[]{"ARCHIVED", storyRepository.countByAuthorIdAndStatus(userId, StoryStatus.ARCHIVED)}
        );
    }
}