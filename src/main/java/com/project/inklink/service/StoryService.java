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

    public StoryService(StoryRepository storyRepository, ValidationService validationService) {
        this.storyRepository = storyRepository;
        this.validationService = validationService;
    }

    public Story createStory(Story story, User author) {
        validationService.validateStoryCreation(story);

        story.setAuthor(author);
        story.setStatus(StoryStatus.DRAFT);
        story.calculateReadingTime();

        return storyRepository.save(story);
    }

    public Story updateStory(Long storyId, Story storyDetails, Long userId) {
        validationService.validateStoryUpdate(storyId, userId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));

        story.setTitle(storyDetails.getTitle());
        story.setContent(storyDetails.getContent());
        story.setExcerpt(storyDetails.getExcerpt());
        story.setCategory(storyDetails.getCategory());
        story.setCoverImage(storyDetails.getCoverImage());
        story.calculateReadingTime();

        return storyRepository.save(story);
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

        return storyRepository.save(story);
    }

    public void deleteStory(Long storyId, Long userId) {
        validationService.validateStoryUpdate(storyId, userId);

        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));

        storyRepository.delete(story);
    }

    public Page<Story> getPublishedStories(Pageable pageable) {
        return storyRepository.findPublishedStoriesWithPagination(pageable);
    }

    public Page<Story> getUserStories(Long userId, StoryStatus status, Pageable pageable) {
        return storyRepository.findByAuthorWithPagination(userId, status, pageable);
    }

    public Optional<Story> getStoryById(Long id) {
        return storyRepository.findById(id);
    }

    public Story incrementViewCount(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new IllegalArgumentException("Story not found"));

        story.incrementViewCount();
        return storyRepository.save(story);
    }

    public List<Story> getTrendingStories(Pageable pageable) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return storyRepository.findTrendingStories(weekAgo, pageable);
    }

    public Page<Story> searchStories(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return getPublishedStories(pageable);
        }
        return storyRepository.searchByTitleOrContent(query.trim(), pageable);
    }

    // FIXED: Added the missing implementation
    public Page<Story> searchByCategory(String category, Pageable pageable) {
        if (category == null || category.trim().isEmpty()) {
            return getPublishedStories(pageable);
        }
        return storyRepository.findByCategoryName(category.trim(), pageable);
    }

    public boolean isStoryOwner(Long storyId, Long userId) {
        return storyRepository.findById(storyId)
                .map(story -> story.getAuthor().getId().equals(userId))
                .orElse(false);
    }
}