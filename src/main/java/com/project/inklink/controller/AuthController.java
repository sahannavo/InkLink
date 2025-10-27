package com.project.inklink.controller;

import com.project.inklink.dto.UserRegistrationDto;
import com.project.inklink.entity.User;
import com.project.inklink.service.UserService;
import com.project.inklink.service.ValidationService;
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
    private final ValidationService validationService;

    public AuthController(UserService userService, ValidationService validationService) {
        this.userService = userService;
        this.validationService = validationService;
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Custom validation
        if (userService.emailExists(userDto.getEmail())) {
            result.rejectValue("email", "error.user", "Email already registered");
        }

        if (userService.usernameExists(userDto.getUsername())) {
            result.rejectValue("username", "error.user", "Username already taken");
        }

        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Passwords do not match");
        }

        if (result.hasErrors()) {
            return "auth/register";
        }

        try {
            // Convert DTO to Entity
            User user = new User();
            user.setUsername(userDto.getUsername());
            user.setEmail(userDto.getEmail());
            user.setPassword(userDto.getPassword());
            user.setRole("USER");
            user.setEnabled(true);

            User registeredUser = userService.registerUser(user);

            redirectAttributes.addFlashAttribute("success",
                    "Registration successful! Please login with your credentials.");
            return "redirect:/login?success";

        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "user/dashboard";
    }
}
