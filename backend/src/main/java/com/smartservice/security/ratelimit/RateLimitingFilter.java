package com.smartservice.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartservice.common.dto.ApiResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.auth.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.auth.requests-per-minute:20}")
    private int requestsPerMinute;

    private final ConcurrentHashMap<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void reset() {
        requestCounts.clear();
    }

    private static class RequestCounter {
        private final long windowStartEpochMinute;
        private final AtomicInteger count;

        public RequestCounter(long epochMinute) {
            this.windowStartEpochMinute = epochMinute;
            this.count = new AtomicInteger(1);
        }
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (!enabled || !isAuthEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        long currentEpochMinute = System.currentTimeMillis() / 60000;

        RequestCounter counter = requestCounts.compute(clientIp, (ip, existingCounter) -> {
            if (existingCounter == null || existingCounter.windowStartEpochMinute != currentEpochMinute) {
                return new RequestCounter(currentEpochMinute);
            }
            existingCounter.count.incrementAndGet();
            return existingCounter;
        });

        // Periodic cleanup if map grows too large
        if (requestCounts.size() > 10000) {
            requestCounts.entrySet().removeIf(entry -> entry.getValue().windowStartEpochMinute < currentEpochMinute);
        }

        if (counter.count.get() > requestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Too many authentication attempts. Please try again later.",
                    "TOO_MANY_REQUESTS"
            );
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthEndpoint(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getServletPath();
        if (!StringUtils.hasText(path)) {
            path = request.getRequestURI();
        }
        return "/api/v1/auth/login".equals(path) || "/api/v1/auth/register".equals(path);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            String[] ips = xForwardedFor.split(",");
            return ips[0].trim();
        }
        return request.getRemoteAddr();
    }
}
