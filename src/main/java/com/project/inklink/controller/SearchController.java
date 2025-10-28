package com.project.inklink.controller;

import com.project.inklink.dto.SearchFilters;
import com.project.inklink.entity.Story;
import com.project.inklink.service.CategoryService;
import com.project.inklink.service.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    private final SearchService searchService;
    private final CategoryService categoryService;

    public SearchController(SearchService searchService, CategoryService categoryService) {
        this.searchService = searchService;
        this.categoryService = categoryService;
    }

    @GetMapping("/advanced")
    public String showAdvancedSearchForm(Model model) {
        model.addAttribute("filters", new SearchFilters());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "search/advanced-search";
    }

    @GetMapping("/results")
    public String advancedSearch(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String[] categories,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer minReadingTime,
            @RequestParam(required = false) Integer maxReadingTime,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        SearchFilters filters = new SearchFilters();
        filters.setQuery(query);

        if (categories != null && categories.length > 0) {
            filters.setCategories(Arrays.asList(categories));
        }

        filters.setAuthor(author);

        // Date parsing logic
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (startDate != null && !startDate.isEmpty()) {
                filters.setStartDate(LocalDateTime.parse(startDate + "T00:00:00"));
            }
            if (endDate != null && !endDate.isEmpty()) {
                filters.setEndDate(LocalDateTime.parse(endDate + "T23:59:59"));
            }
        } catch (Exception e) {
            // Log date parsing error but continue without date filters
            System.err.println("Error parsing dates: " + e.getMessage());
        }

        filters.setMinReadingTime(minReadingTime);
        filters.setMaxReadingTime(maxReadingTime);
        filters.setSortBy(sortBy);

        Pageable pageable = PageRequest.of(page, size);
        Page<Story> results = searchService.advancedSearch(filters, pageable);

        model.addAttribute("stories", results.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", results.getTotalPages());
        model.addAttribute("totalElements", results.getTotalElements());
        model.addAttribute("filters", filters);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "search/advanced-results";
    }

    @GetMapping("/trending")
    public String getTrendingStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        List<Story> trendingStories = searchService.getTrendingStories(pageable);

        model.addAttribute("stories", trendingStories);
        model.addAttribute("currentPage", page);
        model.addAttribute("section", "trending");

        return "search/trending";
    }
}