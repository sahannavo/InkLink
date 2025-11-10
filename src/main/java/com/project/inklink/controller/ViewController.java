package com.project.inklink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String register() {
        return "forward:/register.html";
    }

    @GetMapping("/stories")
    public String stories() {
        return "forward:/stories.html";
    }

    @GetMapping("/story")
    public String story() {
        return "forward:/story.html";
    }

    @GetMapping("/create-story")
    public String createStory() {
        return "forward:/create-story.html";
    }

    @GetMapping("/edit-story")
    public String editStory() {
        return "forward:/edit-story.html";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "forward:/dashboard.html";
    }
}