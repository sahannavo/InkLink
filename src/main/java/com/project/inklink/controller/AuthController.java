package com.project.inklink.controller;

import com.project.inklink.dto.ApiResponse;
import com.project.inklink.dto.AuthRequest;
import com.project.inklink.dto.SignupRequest;
import com.project.inklink.entity.User;
import com.project.inklink.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody AuthRequest authRequest,
                                             HttpServletRequest request) {
        try {
            // Find user
            Optional<User> user = userService.findByUsername(authRequest.getUsername());
            if (user.isEmpty()) {
                user = userService.findByEmail(authRequest.getUsername());
            }

            if (user.isEmpty() || !userService.validatePassword(authRequest.getPassword(), user.get().getPassword())) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "Invalid username or password"));
            }

            // Create session and store user ID
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.get().getId());
            session.setMaxInactiveInterval(30 * 60); // 30 minutes

            System.out.println("DEBUG: Created session for user: " + user.get().getUsername() + ", sessionId: " + session.getId());

            // Return user info (without password)
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.get().getId());
            userInfo.put("username", user.get().getUsername());
            userInfo.put("email", user.get().getEmail());
            userInfo.put("role", user.get().getRole());

            return ResponseEntity.ok(new ApiResponse(true, "Login successful", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            User user = userService.createUser(
                    signupRequest.getUsername(),
                    signupRequest.getEmail(),
                    signupRequest.getPassword()
            );

            // Return user info (without password)
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());

            return ResponseEntity.ok(new ApiResponse(true, "User registered successfully", userInfo));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("DEBUG: Invalidating session: " + session.getId());
            session.invalidate();
        }
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }

    @GetMapping("/me")  // REMOVED 'private' modifier - it should be public!
    public ResponseEntity<ApiResponse> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "Not authenticated"));
        }

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "Not authenticated"));
        }

        Optional<User> user = userService.getUserById(userId);
        if (user.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(new ApiResponse(false, "User not found"));
        }

        // Return user info (without password)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.get().getId());
        userInfo.put("username", user.get().getUsername());
        userInfo.put("email", user.get().getEmail());
        userInfo.put("role", user.get().getRole());

        return ResponseEntity.ok(new ApiResponse(true, "User retrieved", userInfo));
    }
}