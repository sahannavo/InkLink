package com.project.inklink.security;

import com.project.inklink.entity.User;
import com.project.inklink.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class SessionAuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Skip session check for static resources and HTML files
        if (isPublicResource(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("DEBUG: SessionAuthFilter checking: " + requestUri);

        HttpSession session = request.getSession(false);

        if (session != null) {
            System.out.println("DEBUG: Session found: " + session.getId());

            Long userId = (Long) session.getAttribute("userId");
            if (userId != null) {
                System.out.println("DEBUG: User ID found in session: " + userId);

                Optional<User> user = userService.getUserById(userId);
                if (user.isPresent()) {
                    System.out.println("DEBUG: User loaded from DB: " + user.get().getUsername());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user.get().getUsername(),
                                    null,
                                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.get().getRole()))
                            );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("DEBUG: Authentication set for user: " + user.get().getUsername());
                } else {
                    System.out.println("DEBUG: User not found in database for ID: " + userId);
                    session.removeAttribute("userId");
                }
            } else {
                System.out.println("DEBUG: No userId attribute found in session");
            }
        } else {
            System.out.println("DEBUG: No session found for: " + requestUri);
            // Don't block the request - let it through without authentication
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicResource(String requestUri) {
        return requestUri.endsWith(".html") ||
                requestUri.endsWith(".css") ||
                requestUri.endsWith(".js") ||
                requestUri.endsWith(".png") ||
                requestUri.endsWith(".jpg") ||
                requestUri.endsWith(".ico") ||
                requestUri.startsWith("/css/") ||
                requestUri.startsWith("/js/") ||
                requestUri.startsWith("/images/") ||
                requestUri.startsWith("/static/") ||
                requestUri.startsWith("/webjars/") ||
                requestUri.equals("/") ||
                requestUri.equals("/favicon.ico") ||
                requestUri.startsWith("/error");
    }
}