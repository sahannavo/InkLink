package com.project.inklink.service;

import com.project.inklink.dto.AdvancedSearchFilters;
import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.StoryStatus;
import com.project.inklink.repository.StoryRepository;
import com.project.inklink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdvancedSearchService {

    @Autowired
    private StoryRepository storyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnalyticsService analyticsService;

    public Page<Story> advancedSearch(AdvancedSearchFilters filters, Pageable pageable) {
        // Log search for analytics
        if (filters.getQuery() != null && !filters.getQuery().trim().isEmpty()) {
            analyticsService.logSearchQuery(filters.getQuery());
        }

        // Resolve author name to ID if provided
        Long authorId = null;
        if (filters.getAuthor() != null && !filters.getAuthor().trim().isEmpty()) {
            Optional<User> author = userRepository.findByUsername(filters.getAuthor().trim());
            if (author.isPresent()) {
                authorId = author.get().getId();
            } else {
                // Return empty result if author not found
                return Page.empty(pageable);
            }
        }

        // Handle multiple categories
        if (filters.hasCategories() && filters.getCategories().size() > 1) {
            return storyRepository.findByCategories(filters.getCategories(), pageable);
        }

        // Single category search
        String categoryName = filters.hasCategories() && !filters.getCategories().isEmpty()
                ? filters.getCategories().get(0)
                : null;

        return storyRepository.advancedSearch(
                filters.getQuery(),
                categoryName,
                authorId,
                filters.getMinReadingTime(),
                filters.getMaxReadingTime(),
                filters.getStartDate(),
                filters.getEndDate(),
                pageable
        );
    }

    public Page<Story> fullTextSearch(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return storyRepository.findPublishedStoriesWithPagination(pageable);
        }

        analyticsService.logSearchQuery(query);
        return storyRepository.searchByTitleOrContent(query.trim(), pageable);
    }

    public Page<Story> searchByTags(List<String> tags, Pageable pageable) {
        // For now, we'll search in content for tags
        // In a real implementation, you'd have a separate Tag entity
        if (tags == null || tags.isEmpty()) {
            return storyRepository.findPublishedStoriesWithPagination(pageable);
        }

        String tagQuery = String.join(" ", tags);
        return storyRepository.searchByTitleOrContent(tagQuery, pageable);
    }

    public Page<Story> searchStoriesByUserPreferences(Long userId, String query, Pageable pageable) {
        Optional<User> userOpt = userRepository.findByIdWithStories(userId);
        if (userOpt.isEmpty()) {
            return fullTextSearch(query, pageable);
        }

        User user = userOpt.get();

        // Get user's preferred categories from their stories
        List<String> preferredCategories = user.getStories().stream()
                .filter(story -> story.getCategory() != null)
                .map(story -> story.getCategory().getName())
                .distinct()
                .collect(Collectors.toList());

        AdvancedSearchFilters filters = new AdvancedSearchFilters();
        filters.setQuery(query);
        if (!preferredCategories.isEmpty()) {
            filters.setCategories(preferredCategories);
        }

        return advancedSearch(filters, pageable);
    }

    public Page<Story> getRecommendedStories(Long userId, Pageable pageable) {
        Optional<User> userOpt = userRepository.findByIdWithStories(userId);
        if (userOpt.isEmpty()) {
            return storyRepository.findPopularStories(LocalDateTime.now().minusDays(30), pageable);
        }

        User user = userOpt.get();

        // Simple recommendation based on user's categories
        List<String> userCategories = user.getStories().stream()
                .filter(story -> story.getCategory() != null)
                .map(story -> story.getCategory().getName())
                .distinct()
                .collect(Collectors.toList());

        if (userCategories.isEmpty()) {
            return storyRepository.findPopularStories(LocalDateTime.now().minusDays(30), pageable);
        }

        return storyRepository.findByCategories(userCategories, pageable);
    }

    public Page<Story> getTrendingStoriesByCategory(String category, Pageable pageable) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

        if (category == null || category.trim().isEmpty()) {
            return storyRepository.findTrendingStories(weekAgo, pageable);
        }

        // Use the existing findByCategoryName method
        return storyRepository.findByCategoryName(category.trim(), pageable);
    }

    public List<Story> getSimilarStories(Long storyId, int limit) {
        Optional<Story> storyOpt = storyRepository.findById(storyId);
        if (storyOpt.isEmpty()) {
            return List.of();
        }

        Story story = storyOpt.get();

        if (story.getCategory() == null) {
            return List.of();
        }

        // Find stories with same category
        return storyRepository.findRelatedStories(
                story.getCategory().getName(),
                storyId,
                Pageable.ofSize(limit)
        ).getContent();
    }
}