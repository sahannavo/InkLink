package com.project.inklink.security;

import com.project.inklink.entity.User;
import com.project.inklink.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find user by username first
        Optional<User> user = userRepository.findByUsername(username);

        // If not found by username, try by email
        if (user.isEmpty()) {
            user = userRepository.findByEmail(username);
        }

        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username or email: " + username);
        }

        return new CustomUserDetails(user.get());
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        return new CustomUserDetails(user);
    }

    @Transactional(readOnly = true)
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username) || userRepository.existsByEmail(username);
    }

    @Transactional(readOnly = true)
    public User getUserByUsernameOrEmail(String identifier) {
        Optional<User> user = userRepository.findByUsername(identifier);
        if (user.isEmpty()) {
            user = userRepository.findByEmail(identifier);
        }
        return user.orElse(null);
    }
}