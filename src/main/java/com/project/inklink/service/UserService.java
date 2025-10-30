package com.project.inklink.service;

import com.project.inklink.entity.User;
import com.project.inklink.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        // Email functionality commented out for now - can be enabled later
        System.out.println("User registered successfully: " + savedUser.getUsername() + " (" + savedUser.getEmail() + ")");

        return savedUser;
    }

    public User updateUserProfile(Long userId, User userDetails) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Check if email is being changed and if it conflicts
        if (!user.getEmail().equals(userDetails.getEmail()) &&
                userRepository.existsByEmail(userDetails.getEmail())) {
            throw new RuntimeException("Email already exists: " + userDetails.getEmail());
        }

        // Check if username is being changed and if it conflicts
        if (!user.getUsername().equals(userDetails.getUsername()) &&
                userRepository.existsByUsername(userDetails.getUsername())) {
            throw new RuntimeException("Username already exists: " + userDetails.getUsername());
        }

        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setBio(userDetails.getBio()); // NOW WORKS - bio field exists
        user.setAvatarUrl(userDetails.getAvatarUrl());

        return userRepository.save(user);
    }

    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (newPassword.length() < 6) {
            throw new RuntimeException("New password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    public Long getPublishedStoryCount(Long userId) {
        return userRepository.countPublishedStoriesByUser(userId);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null); // or throw an exception
    }
}