package com.project.inklink.config;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PaginationConfig {

    // Default pagination for homepage
    public Pageable getDefaultPageable() {
        return PageRequest.of(0, 10, Sort.by("publishedAt").descending());
    }

    // Pagination with custom sorting
    public Pageable getPageable(int page, int size, String sortBy) {
        Sort sort = getSort(sortBy);
        return PageRequest.of(page, size, sort);
    }

    // For user stories with status filtering
    public Pageable getUserStoriesPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by("createdAt").descending());
    }

    // For search results
    public Pageable getSearchPageable(int page, int size) {
        return PageRequest.of(page, size);
    }

    // Sorting logic for different options
    private Sort getSort(String sortBy) {
        switch (sortBy != null ? sortBy.toLowerCase() : "newest") {
            case "popular":
                return Sort.by("viewCount").descending()
                        .and(Sort.by("publishedAt").descending());
            case "readingtime":
                return Sort.by("readingTime").ascending();
            case "trending":
                return Sort.by("viewCount").descending()
                        .and(Sort.by("publishedAt").descending());
            case "newest":
            default:
                return Sort.by("publishedAt").descending();
        }
    }

    // For trending stories (last 7 days)
    public Pageable getTrendingPageable() {
        return PageRequest.of(0, 5, Sort.by("viewCount").descending());
    }

    // For category pages
    public Pageable getCategoryPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by("publishedAt").descending());
    }

    // For user bookmarks and likes
    public Pageable getUserContentPageable(int page, int size) {
        return PageRequest.of(page, size, Sort.by("createdAt").descending());
    }
}