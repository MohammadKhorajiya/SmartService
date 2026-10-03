package com.smartservice.security.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartservice.common.dto.ApiResponse;
import com.smartservice.security.jwt.CookieUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
public class CsrfValidationFilter extends OncePerRequestFilter {

    private static final Set<String> STATE_CHANGING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Validate double-submit CSRF token on state-changing requests to /api/v1/auth endpoints
        if (path.startsWith("/api/v1/auth/") && STATE_CHANGING_METHODS.contains(method.toUpperCase())) {
            String csrfCookie = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (CookieUtils.CSRF_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
                        csrfCookie = cookie.getValue();
                        break;
                    }
                }
            }

            // If a CSRF cookie was issued to the client, double-submit header matching is required
            if (csrfCookie != null && !csrfCookie.isBlank()) {
                String csrfHeader = request.getHeader(CookieUtils.CSRF_HEADER_NAME);
                if (csrfHeader == null || !csrfHeader.equals(csrfCookie)) {
                    log.warn("CSRF validation failed for request {} {}", method, path);
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    ApiResponse<Void> errResponse = ApiResponse.error("CSRF token validation failed", "CSRF_ERROR");
                    response.getWriter().write(objectMapper.writeValueAsString(errResponse));
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
