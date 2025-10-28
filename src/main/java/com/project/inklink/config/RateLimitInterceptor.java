package com.project.inklink.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, RequestTracker> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100; // per minute
    private static final long TIME_WINDOW = TimeUnit.MINUTES.toMillis(1);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String path = request.getRequestURI();

        String key = clientIp + ":" + path;
        RequestTracker tracker = requestCounts.computeIfAbsent(key, k -> new RequestTracker());

        if (tracker.isRateLimited()) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return false;
        }

        tracker.recordRequest();
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }

    private static class RequestTracker {
        private long windowStart;
        private int requestCount;

        RequestTracker() {
            this.windowStart = System.currentTimeMillis();
            this.requestCount = 0;
        }

        synchronized boolean isRateLimited() {
            long currentTime = System.currentTimeMillis();

            // Reset if time window has passed
            if (currentTime - windowStart > TIME_WINDOW) {
                windowStart = currentTime;
                requestCount = 0;
            }

            return requestCount >= MAX_REQUESTS;
        }

        synchronized void recordRequest() {
            requestCount++;
        }
    }
}