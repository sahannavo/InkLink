package com.project.inklink.config;

import com.project.inklink.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private static final String REMEMBER_ME_KEY = "inklink-remember-me-key-2025";

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints
                        .requestMatchers(
                                "/", "/home", "/register", "/login",
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/stories", "/stories/view/**", "/api/stories/public/**",
                                "/categories", "/api/categories/**",
                                "/error/**", "/h2-console/**",
                                "/favicon.ico", "/robots.txt"
                        ).permitAll()
                        // User endpoints
                        .requestMatchers(
                                "/profile/**", "/stories/create", "/stories/my",
                                "/stories/edit/**", "/stories/delete/**",
                                "/api/stories/user/**", "/api/user/**",
                                "/dashboard",
                                "/api/comments/**",
                                "/api/notifications/**"
                        ).authenticated()
                        // Admin endpoints
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeServices(rememberMeServices())
                        .key(REMEMBER_ME_KEY)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .expiredUrl("/login?expired=true")
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers(
                                "/h2-console/**",
                                "/api/public/**"
                        )
                )
                .headers(headers -> headers
                        // Frame options for H2 console
                        .frameOptions(frame -> frame.sameOrigin())
                        // Content Security Policy
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; " +
                                        "script-src 'self' https://cdn.jsdelivr.net; " +
                                        "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                                        "img-src 'self' data: blob: https://cdn.jsdelivr.net; " +
                                        "font-src 'self' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; " +
                                        "connect-src 'self'; " +
                                        "frame-src 'self'; " +
                                        "object-src 'none'; " +
                                        "base-uri 'self'")
                        )
                        // HTTP Strict Transport Security
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000)
                        )
                        // XSS Protection
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                        )
                        // Content Type Options
                        .contentTypeOptions(contentType -> {})
                        // Referrer Policy
                        .referrerPolicy(referrer -> referrer
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                        )
                )
                .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        authProvider.setHideUserNotFoundExceptions(false);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // Check for redirect parameter
            String redirectUrl = request.getParameter("redirect");
            if (redirectUrl != null && !redirectUrl.isEmpty() &&
                    !redirectUrl.contains("//") && redirectUrl.startsWith("/")) {
                response.sendRedirect(redirectUrl);
                return;
            }

            // Check if the request came from the default login page or was direct
            String referer = request.getHeader("Referer");
            if (referer != null && referer.contains("/login")) {
                // Redirect based on role
                boolean isAdmin = authentication.getAuthorities().stream()
                        .anyMatch(grantedAuthority ->
                                grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

                if (isAdmin) {
                    response.sendRedirect("/admin/dashboard");
                } else {
                    response.sendRedirect("/dashboard");
                }
            } else {
                // Default redirect to home
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public TokenBasedRememberMeServices rememberMeServices() {
        TokenBasedRememberMeServices rememberMe =
                new TokenBasedRememberMeServices(REMEMBER_ME_KEY, userDetailsService);
        rememberMe.setTokenValiditySeconds(1209600); // 2 weeks
        rememberMe.setParameter("remember-me");
        rememberMe.setCookieName("remember-me");
        rememberMe.setAlwaysRemember(false);
        return rememberMe;
    }
}