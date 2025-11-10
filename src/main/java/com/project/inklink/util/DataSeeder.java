package com.project.inklink.util;

import com.project.inklink.entity.Story;
import com.project.inklink.entity.User;
import com.project.inklink.entity.enums.StoryGenre;
import com.project.inklink.entity.enums.StoryStatus;
import com.project.inklink.service.StoryService;
import com.project.inklink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private StoryService storyService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("DataSeeder started...");

        try {
            if (userService.findByUsername("author1").isEmpty()) {
                System.out.println("Creating sample users...");

                User author1 = userService.createUser("author1", "author1@example.com", "password123");
                User author2 = userService.createUser("author2", "author2@example.com", "password123");

                System.out.println("Creating sample stories...");

                Story story1 = new Story();
                story1.setTitle("The Mysterious Forest");
                story1.setContent("Once upon a time, in a mysterious forest...");
                story1.setGenre(StoryGenre.FANTASY);
                story1.setStatus(StoryStatus.PUBLISHED);
                story1.setAuthor(author1);
                storyService.createStory(story1);

                Story story2 = new Story();
                story2.setTitle("Space Adventure");
                story2.setContent("The spaceship traveled through the galaxy...");
                story2.setGenre(StoryGenre.SCI_FI);
                story2.setStatus(StoryStatus.PUBLISHED);
                story2.setAuthor(author2);
                storyService.createStory(story2);

                System.out.println("Sample data created successfully!");
            } else {
                System.out.println("Sample data already exists.");
            }
        } catch (Exception e) {
            System.err.println("Error in DataSeeder: " + e.getMessage());
            e.printStackTrace();
        }
    }
}