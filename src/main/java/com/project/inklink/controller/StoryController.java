package com.project.inklink.controller;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.StoryStatus;
import com.project.inklink.service.CategoryService;
import com.project.inklink.service.StoryService;
import com.project.inklink.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/stories")
public class StoryController {

    private final StoryService storyService;
    private final UserService userService;
    private final CategoryService categoryService;

    public StoryController(StoryService storyService, UserService userService, CategoryService categoryService) {
        this.storyService = storyService;  // CORRECT - use the injected instance
        this.userService = userService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listStories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "newest") String sort,
            Model model) {

        Pageable pageable = createPageable(page, size, sort);
        Page<Story> storiesPage;

        if (query != null && !query.isEmpty()) {
            storiesPage = storyService.searchStories(query, pageable);
            model.addAttribute("query", query);
        } else if (category != null && !category.isEmpty()) {
            storiesPage = storyService.searchByCategory(category, pageable);
            model.addAttribute("selectedCategory", category);
        } else {
            storiesPage = storyService.getPublishedStories(pageable);
        }

        model.addAttribute("stories", storiesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", storiesPage.getTotalPages());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("sortBy", sort);

        return "stories/list";
    }

    @GetMapping("/{id}")
    public String viewStory(@PathVariable Long id, Model model) {
        Optional<Story> storyOpt = storyService.getStoryById(id);

        if (storyOpt.isPresent()) {
            Story story = storyOpt.get();
            if (StoryStatus.PUBLISHED.name().equals(story.getStatus())) {
                storyService.incrementViewCount(id);
            }
            model.addAttribute("story", story);
            return "stories/view";
        }

        return "error/404";
    }
    @GetMapping("/create")
    public String showCreateForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {
            model.addAttribute("story", new Story());
            model.addAttribute("categories", categoryService.getAllCategories());
            return "stories/create";
        }

        return "redirect:/login";
    }

    @PostMapping("/create")
    public String createStory(@ModelAttribute Story story,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        String email = userDetails.getUsername();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {
            try {
                Story createdStory = storyService.createStory(story, userOpt.get());
                return "redirect:/stories/" + createdStory.getId();
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                model.addAttribute("categories", categoryService.getAllCategories());
                return "stories/create";
            }
        }

        return "redirect:/login";
    }

    @GetMapping("/my")
    public String myStories(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            Model model) {
        String email = userDetails.getUsername();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Story> stories = storyService.getUserStories(user.getId(), null, pageable);

            model.addAttribute("stories", stories.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", stories.getTotalPages());

            return "stories/my-stories";
        }

        return "redirect:/login";
    }

    private Pageable createPageable(int page, int size, String sort) {
        Sort sortObj;
        switch (sort) {
            case "popular":
                sortObj = Sort.by("viewCount").descending().and(Sort.by("publishedAt").descending());
                break;
            case "readingtime":
                sortObj = Sort.by("readingTime").ascending();
                break;
            case "newest":
            default:
                sortObj = Sort.by("publishedAt").descending();
        }
        return PageRequest.of(page, size, sortObj);
    }
}
