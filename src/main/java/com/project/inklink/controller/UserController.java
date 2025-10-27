package com.project.inklink.controller;

import com.project.inklink.entity.User;
import com.project.inklink.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            return "user/profile";
        }

        return "redirect:/login";
    }

    @GetMapping("/{id}")
    public String showUserProfile(@PathVariable Long id, Model model) {
        Optional<User> userOpt = userService.findById(id);

        if (userOpt.isPresent()) {
            model.addAttribute("user", userOpt.get());
            // Add user's stories, stats, etc.
            return "user/public-profile";
        }

        return "error/404";
    }
}
