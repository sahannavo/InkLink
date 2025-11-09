package com.project.inklink.security;

import com.project.inklink.entity.User;
import com.project.inklink.entity.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Convert UserRole to Spring Security authority
        String role = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Account never expires in our system
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Account never gets locked in our system
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Credentials never expire in our system
    }

    @Override
    public boolean isEnabled() {
        return true; // All users are enabled in our system
    }

    // Custom methods to access user entity
    public User getUser() {
        return user;
    }

    public Long getUserId() {
        return user.getId();
    }

    public UserRole getUserRole() {
        return user.getRole();
    }

    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String toString() {
        return "CustomUserDetails{" +
                "userId=" + user.getId() +
                ", username='" + user.getUsername() + '\'' +
                ", role=" + user.getRole() +
                '}';
    }
}