package com.project.inklink.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AdvancedSearchFilters {
    private String query;
    private List<String> categories;
    private String author;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer minReadingTime;
    private Integer maxReadingTime;
    private String sortBy; // "newest", "popular", "readingTime", "trending"
    private Boolean featuredOnly;
    private List<String> tags;
    private Integer minWordCount;
    private Integer maxWordCount;

    // Constructors
    public AdvancedSearchFilters() {}

    public AdvancedSearchFilters(String query) {
        this.query = query;
    }

    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }

    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }

    public Integer getMinReadingTime() { return minReadingTime; }
    public void setMinReadingTime(Integer minReadingTime) { this.minReadingTime = minReadingTime; }

    public Integer getMaxReadingTime() { return maxReadingTime; }
    public void setMaxReadingTime(Integer maxReadingTime) { this.maxReadingTime = maxReadingTime; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public Boolean getFeaturedOnly() { return featuredOnly; }
    public void setFeaturedOnly(Boolean featuredOnly) { this.featuredOnly = featuredOnly; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Integer getMinWordCount() { return minWordCount; }
    public void setMinWordCount(Integer minWordCount) { this.minWordCount = minWordCount; }

    public Integer getMaxWordCount() { return maxWordCount; }
    public void setMaxWordCount(Integer maxWordCount) { this.maxWordCount = maxWordCount; }

    // Validation methods
    public boolean hasCategories() {
        return categories != null && !categories.isEmpty();
    }

    public boolean hasDateRange() {
        return startDate != null && endDate != null;
    }

    public boolean hasReadingTimeRange() {
        return minReadingTime != null || maxReadingTime != null;
    }

    public boolean hasWordCountRange() {
        return minWordCount != null || maxWordCount != null;
    }

    public boolean hasTags() {
        return tags != null && !tags.isEmpty();
    }

    // Helper methods for building search queries
    public String buildSearchDescription() {
        StringBuilder description = new StringBuilder();

        if (query != null && !query.isEmpty()) {
            description.append("Search: ").append(query);
        }

        if (hasCategories()) {
            if (description.length() > 0) description.append(" | ");
            description.append("Categories: ").append(String.join(", ", categories));
        }

        if (author != null && !author.isEmpty()) {
            if (description.length() > 0) description.append(" | ");
            description.append("Author: ").append(author);
        }

        if (hasDateRange()) {
            if (description.length() > 0) description.append(" | ");
            description.append("Date Range: ").append(startDate.toLocalDate())
                    .append(" to ").append(endDate.toLocalDate());
        }

        return description.toString();
    }
}
