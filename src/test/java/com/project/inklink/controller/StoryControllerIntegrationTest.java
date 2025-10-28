package com.project.inklink.controller;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.enums.StoryStatus;
import com.project.inklink.service.StoryService;
import com.project.inklink.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class StoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoryService storyService;

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setRole("USER");
        testUser.setEnabled(true);

        userService.registerUser(testUser);
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateStory_ValidData_ShouldRedirect() throws Exception {
        mockMvc.perform(post("/stories/create")
                        .with(csrf())
                        .param("title", "Test Story Title")
                        .param("content", "This is a test story content with more than 100 characters to meet validation requirements for published stories. This should be sufficient length.")
                        .param("excerpt", "Test excerpt for the story"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/stories/*"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testCreateStory_InvalidData_ShouldReturnForm() throws Exception {
        mockMvc.perform(post("/stories/create")
                        .with(csrf())
                        .param("title", "") // Empty title
                        .param("content", "Short") // Too short content
                        .param("excerpt", "Test excerpt"))
                .andExpect(status().isOk())
                .andExpect(view().name("stories/create"))
                .andExpect(model().attributeHasErrors("story"));
    }

    @Test
    void testViewPublishedStory_ShouldReturnStoryPage() throws Exception {
        // Create a published story
        Story story = new Story();
        story.setTitle("Published Integration Test Story");
        story.setContent("This is a valid story content for integration testing. It needs to be long enough to pass validation. ".repeat(3));
        story.setAuthor(testUser);
        story.setStatus(StoryStatus.PUBLISHED);

        Story savedStory = storyService.createStory(story, testUser);

        mockMvc.perform(get("/stories/" + savedStory.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("stories/view"))
                .andExpect(model().attributeExists("story"));
    }

    @Test
    void testViewNonExistentStory_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/stories/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testGetMyStories_AuthenticatedUser_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/stories/my"))
                .andExpect(status().isOk())
                .andExpect(view().name("stories/my-stories"))
                .andExpect(model().attributeExists("stories"));
    }

    @Test
    void testGetMyStories_Unauthenticated_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/stories/my"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}