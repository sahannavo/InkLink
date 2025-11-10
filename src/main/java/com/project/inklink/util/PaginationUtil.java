package com.project.inklink.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class PaginationUtil {

    // Default pagination values
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT = "createdAt,desc";

    /**
     * Create Pageable object with validation
     */
    public static Pageable createPageable(Integer page, Integer size, String sort) {
        int validPage = (page == null || page < 0) ? DEFAULT_PAGE : page;
        int validSize = (size == null || size <= 0) ? DEFAULT_SIZE : Math.min(size, MAX_PAGE_SIZE);
        String validSort = (sort == null || sort.trim().isEmpty()) ? DEFAULT_SORT : sort;

        return createPageableFromSort(validPage, validSize, validSort);
    }

    /**
     * Create Pageable from sort string (field,direction)
     */
    public static Pageable createPageableFromSort(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String sortBy = sortParams[0];
        Sort.Direction direction = Sort.Direction.DESC; // Default direction

        if (sortParams.length > 1) {
            String directionStr = sortParams[1].toLowerCase();
            if ("asc".equals(directionStr)) {
                direction = Sort.Direction.ASC;
            }
        }

        // Validate and sanitize sort field
        sortBy = sanitizeSortField(sortBy);

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    /**
     * Sanitize sort field to prevent SQL injection
     */
    private static String sanitizeSortField(String field) {
        // Remove any non-alphanumeric characters except underscores
        return field.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * Create pagination response metadata
     */
    public static Map<String, Object> createPaginationMetadata(Page<?> page) {
        Map<String, Object> metadata = new HashMap<>();

        metadata.put("currentPage", page.getNumber());
        metadata.put("totalPages", page.getTotalPages());
        metadata.put("totalItems", page.getTotalElements());
        metadata.put("pageSize", page.getSize());
        metadata.put("hasNext", page.hasNext());
        metadata.put("hasPrevious", page.hasPrevious());
        metadata.put("isFirst", page.isFirst());
        metadata.put("isLast", page.isLast());

        return metadata;
    }

    /**
     * Create pagination links for HATEOAS
     */
    public static Map<String, String> createPaginationLinks(Page<?> page, String baseUrl) {
        Map<String, String> links = new HashMap<>();
        int currentPage = page.getNumber();
        int totalPages = page.getTotalPages();

        // Self link
        links.put("self", createPageLink(baseUrl, currentPage, page.getSize()));

        // First link
        links.put("first", createPageLink(baseUrl, 0, page.getSize()));

        // Last link
        if (totalPages > 0) {
            links.put("last", createPageLink(baseUrl, totalPages - 1, page.getSize()));
        }

        // Next link
        if (page.hasNext()) {
            links.put("next", createPageLink(baseUrl, currentPage + 1, page.getSize()));
        }

        // Previous link
        if (page.hasPrevious()) {
            links.put("prev", createPageLink(baseUrl, currentPage - 1, page.getSize()));
        }

        return links;
    }

    /**
     * Create page link with query parameters
     */
    private static String createPageLink(String baseUrl, int page, int size) {
        return String.format("%s?page=%d&size=%d", baseUrl, page, size);
    }

    /**
     * Validate page parameters
     */
    public static void validatePageParameters(Integer page, Integer size) {
        if (page != null && page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size != null && (size <= 0 || size > MAX_PAGE_SIZE)) {
            throw new IllegalArgumentException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }
    }

    /**
     * Get start and end indices for manual pagination
     */
    public static int[] getPaginationIndices(int page, int size, int totalItems) {
        int startIndex = page * size;
        int endIndex = Math.min(startIndex + size, totalItems);

        if (startIndex > totalItems) {
            startIndex = totalItems;
            endIndex = totalItems;
        }

        return new int[]{startIndex, endIndex};
    }

    /**
     * Paginate a list manually (for in-memory collections)
     */
    public static <T> List<T> paginateList(List<T> list, int page, int size) {
        int[] indices = getPaginationIndices(page, size, list.size());
        return list.subList(indices[0], indices[1]);
    }

    /**
     * Calculate total pages
     */
    public static int calculateTotalPages(long totalItems, int pageSize) {
        if (pageSize <= 0) return 0;
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    /**
     * Get available sort options for different entities
     */
    public static Map<String, List<String>> getAvailableSortOptions(String entityType) {
        Map<String, List<String>> sortOptions = new HashMap<>();

        switch (entityType.toLowerCase()) {
            case "story":
                sortOptions.put("fields", List.of("title", "createdAt", "updatedAt", "readCount", "genre"));
                sortOptions.put("directions", List.of("asc", "desc"));
                break;
            case "user":
                sortOptions.put("fields", List.of("username", "createdAt", "email"));
                sortOptions.put("directions", List.of("asc", "desc"));
                break;
            case "comment":
                sortOptions.put("fields", List.of("createdAt", "content"));
                sortOptions.put("directions", List.of("asc", "desc"));
                break;
            default:
                sortOptions.put("fields", List.of("createdAt", "id"));
                sortOptions.put("directions", List.of("asc", "desc"));
        }

        return sortOptions;
    }

    /**
     * Build sort parameter from field and direction
     */
    public static String buildSortParameter(String field, String direction) {
        if (field == null || field.trim().isEmpty()) {
            field = "createdAt";
        }
        if (direction == null || direction.trim().isEmpty()) {
            direction = "desc";
        }
        return field + "," + direction;
    }
}