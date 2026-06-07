package com.stripe.payflow.infrastructure.web;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String username) {
        return cache.computeIfAbsent(username, this::newBucket);
    }

    private Bucket newBucket(String username) {
        // Limit to 10 requests per minute
        Bandwidth limit = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // We only rate limit the transfer endpoint for this demonstration
        if (request.getRequestURI().contains("/transfers")) {
            // Assume the user is authenticated via JWT and their username is the remote user
            String username = request.getRemoteUser();
            if (username == null) {
                // If not authenticated (shouldn't happen due to security filter, but safe fallback)
                username = request.getRemoteAddr();
            }

            Bucket tokenBucket = resolveBucket(username);
            if (tokenBucket.tryConsume(1)) {
                return true;
            } else {
                response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                        "Too many requests. Please wait before trying again.");
                return false;
            }
        }
        return true;
    }
}
