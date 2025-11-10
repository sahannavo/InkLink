package com.project.inklink.service;


import com.project.inklink.entity.User;
import com.project.inklink.entity.enums.UserRole;
import com.project.inklink.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    // Authentication methods
    public User authenticate(String username, String password) {
        // Try username first, then email
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(username);
        }

        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user.get();
        }

        throw new RuntimeException("Invalid username or password");
    }

    public User register(String username, String email, String password) {
        return userService.createUser(username, email, password);
    }

    // Session management
    public void createSession(User user, HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.setAttribute("user", user);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes
    }

    public void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public User getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    public boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    // Authorization checks
    public boolean isAuthorized(User user, Long resourceOwnerId) {
        if (user == null) return false;
        return user.getId().equals(resourceOwnerId) || user.getRole() == UserRole.ADMIN;
    }

    public boolean canModifyStory(User user, Long storyAuthorId) {
        return isAuthorized(user, storyAuthorId);
    }

    public boolean canModifyComment(User user, Long commentAuthorId, Long storyAuthorId) {
        if (user == null) return false;
        return user.getId().equals(commentAuthorId) ||
                user.getId().equals(storyAuthorId) ||
                user.getRole() == UserRole.ADMIN;
    }

    // Password management
    public boolean validateCurrentPassword(User user, String currentPassword) {
        return passwordEncoder.matches(currentPassword, user.getPassword());
    }

    public User changeUserPassword(User user, String newPassword) {
        return userService.changePassword(user.getId(), newPassword);
    }

    // Security utilities
    public void validateUserAccess(User user, Long targetUserId) {
        if (user == null) {
            throw new RuntimeException("Authentication required");
        }

        if (!user.getId().equals(targetUserId) && user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Access denied");
        }
    }

    public void validateAdminAccess(User user) {
        if (user == null || user.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("Admin access required");
        }
    }

    // Session utilities
    public void refreshSession(User user, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute("user", user);
        }
    }

    public int getSessionTimeout() {
        return 30 * 60; // 30 minutes in seconds
    }
}
