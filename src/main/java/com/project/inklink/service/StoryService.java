package com.project.inklink.service;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.entity.enums.StoryGenre;
import com.project.inklink.entity.enums.StoryStatus;
import com.project.inklink.entity.enums.UserRole;
import com.project.inklink.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StoryService {

    @Autowired
    private StoryRepository storyRepository;

    // Basic CRUD operations
    public List<Story> getAllStories() {
        return storyRepository.findAll();
    }

    public Optional<Story> getStoryById(Long id) {
        return storyRepository.findById(id);
    }

    public Story createStory(Story story) {
        // Validate unique title for author
        if (storyRepository.existsByTitleAndAuthor(story.getTitle(), story.getAuthor())) {
            throw new RuntimeException("You already have a story with this title");
        }
        return storyRepository.save(story);
    }

    public Story updateStory(Story story) {
        return storyRepository.save(story);
    }

    public void deleteStory(Long id) {
        storyRepository.deleteById(id);
    }

    // Publishing operations
    public Story publishStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        story.setStatus(StoryStatus.PUBLISHED);
        return storyRepository.save(story);
    }

    public Story unpublishStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new RuntimeException("Story not found"));

        story.setStatus(StoryStatus.DRAFT);
        return storyRepository.save(story);
    }

    // Reading and analytics
    public void incrementReadCount(Long storyId) {
        storyRepository.findById(storyId).ifPresent(story -> {
            story.setReadCount(story.getReadCount() + 1);
            storyRepository.save(story);
        });
    }

    // Query methods with pagination
    public Page<Story> getPublishedStories(Pageable pageable) {
        return storyRepository.findByStatus(StoryStatus.PUBLISHED, pageable);
    }

    public Page<Story> searchStories(String search, Pageable pageable) {
        return storyRepository.searchPublishedStories(search, pageable);
    }

    public Page<Story> getStoriesByGenre(StoryGenre genre, Pageable pageable) {
        return storyRepository.findByGenreAndStatus(genre.toString(), pageable);
    }

    public Page<Story> getPopularStories(Pageable pageable) {
        return storyRepository.findPopularStories(pageable);
    }

    public Page<Story> getRecentStories(Pageable pageable) {
        return storyRepository.findRecentStories(pageable);
    }

    // User-specific operations
    public List<Story> getUserStories(User author) {
        return storyRepository.findByAuthor(author);
    }

    public List<Story> getUserStoriesByStatus(User author, StoryStatus status) {
        return storyRepository.findByAuthorAndStatus(author, status);
    }

    public Page<Story> getUserStories(User author, Pageable pageable) {
        return storyRepository.findByAuthor(author, pageable);
    }

    // Authorization checks
    public boolean isStoryAuthor(Long storyId, Long userId) {
        return storyRepository.findById(storyId)
                .map(story -> story.getAuthor().getId().equals(userId))
                .orElse(false);
    }

    public boolean canEditStory(Long storyId, User user) {
        return storyRepository.findById(storyId)
                .map(story -> story.getAuthor().getId().equals(user.getId()) || user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    // Analytics
    public Long getPublishedStoryCount() {
        return storyRepository.count();
    }

    public Long getUserPublishedStoryCount(User author) {
        return storyRepository.countPublishedStoriesByAuthor(author);
    }

    public Long getTotalReadCount(User author) {
        return getUserStories(author).stream()
                .mapToLong(Story::getReadCount)
                .sum();
    }

    // Recent activity
    public List<Story> getRecentStories(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return storyRepository.findByStatusAndCreatedAtAfter(StoryStatus.PUBLISHED, since, Pageable.unpaged())
                .getContent();
    }
}