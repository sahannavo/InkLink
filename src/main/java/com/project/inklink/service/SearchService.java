package com.project.inklink.service;

import com.project.inklink.dto.SearchFilters;
import com.project.inklink.entity.Story;
import com.project.inklink.repository.StoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private StoryRepository storyRepository;

    public Page<Story> searchStories(String query, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            return storyRepository.findPublishedStoriesWithPagination(pageable);
        }
        return storyRepository.searchByTitleOrContent(query.trim(), pageable);
    }

    public Page<Story> advancedSearch(SearchFilters filters, Pageable pageable) {
        String categoryName = filters.hasCategories() && filters.getCategories().size() == 1
                ? filters.getCategories().get(0)
                : null;

        Long authorId = null; // You might want to resolve author name to ID
        Integer minReadingTime = filters.getMinReadingTime();
        Integer maxReadingTime = filters.getMaxReadingTime();
        LocalDateTime startDate = filters.getStartDate();
        LocalDateTime endDate = filters.getEndDate();

        // If multiple categories are selected, use different method
        if (filters.hasCategories() && filters.getCategories().size() > 1) {
            return storyRepository.findByCategories(filters.getCategories(), pageable);
        }

        return storyRepository.advancedSearch(
                categoryName,
                authorId,
                minReadingTime,
                maxReadingTime,
                startDate,
                endDate,
                pageable
        );
    }

    public Page<Story> searchByCategory(String category, Pageable pageable) {
        return storyRepository.findByCategory(category, pageable);
    }

    public Page<Story> searchByAuthor(Long authorId, Pageable pageable) {
        return storyRepository.findByAuthorWithPagination(authorId,
                com.project.inklink.enums.StoryStatus.PUBLISHED, pageable);
    }

    public Page<Story> searchByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return storyRepository.findByPublishedDateBetween(startDate, endDate, pageable);
    }

    public Page<Story> searchByReadingTime(Integer maxReadingTime, Pageable pageable) {
        return storyRepository.findByReadingTimeLessThanEqual(maxReadingTime, pageable);
    }

    public List<Story> getTrendingStories(Pageable pageable) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return storyRepository.findTrendingStories(weekAgo, pageable);
    }

    public Page<Story> getPopularStories(Pageable pageable) {
        return storyRepository.findPopularStories(pageable);
    }
}