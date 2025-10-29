package com.project.inklink.controller;

import com.project.inklink.dto.UserRegistrationDto;
import com.project.inklink.entity.User;
import com.project.inklink.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Check if user attribute already exists to avoid overwriting in case of errors
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        // Custom validation - only check if there are no field validation errors first
        if (!result.hasFieldErrors("email") && userService.emailExists(userDto.getEmail())) {
            result.rejectValue("email", "error.user", "Email already registered");
        }

        if (!result.hasFieldErrors("username") && userService.usernameExists(userDto.getUsername())) {
            result.rejectValue("username", "error.user", "Username already taken");
        }

        if (!result.hasFieldErrors("confirmPassword") &&
                !userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }

        if (result.hasErrors()) {
            // Add the user DTO back to flash attributes to preserve form data
            redirectAttributes.addFlashAttribute("user", userDto);
            // Add BindingResult to flash attributes
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
            return "redirect:/register";
        }

        try {
            // Convert DTO to Entity
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPassword(userDto.getPassword());
            user.setRole("USER");
            user.setEnabled(true);

            userService.registerUser(user);

            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please login with your credentials.");
            return "redirect:/login?success";

        } catch (Exception e) {
            // Add error message and preserve form data
            redirectAttributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            redirectAttributes.addFlashAttribute("user", userDto);
            return "redirect:/register";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "user/dashboard";
    }
}