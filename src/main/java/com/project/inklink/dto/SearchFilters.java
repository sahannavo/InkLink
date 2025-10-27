package com.project.inklink.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SearchFilters {
        private String query;
        private List<String> categories;
        private String author;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer minReadingTime;
        private Integer maxReadingTime;
        private String sortBy; // "newest", "popular", "readingTime"

        // Constructors
        public SearchFilters() {}

        public SearchFilters(String query) {
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
}