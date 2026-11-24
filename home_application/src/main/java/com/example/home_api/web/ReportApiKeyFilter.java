package com.example.home_api.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class ReportApiKeyFilter extends OncePerRequestFilter {
    static final String HEADER_NAME = "X-Report-Api-Key";
    static final int MINIMUM_KEY_LENGTH = 32;
    private final String configuredKey;

    public ReportApiKeyFilter(@Value("${report.api.key:}") String configuredKey) {
        this.configuredKey = configuredKey == null ? "" : configuredKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.equals("/api/reports") || path.startsWith("/api/reports/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        if (configuredKey.length() < MINIMUM_KEY_LENGTH) {
            error(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE, "report_api_not_configured");
            return;
        }
        String supplied = request.getHeader(HEADER_NAME);
        if (supplied == null || !MessageDigest.isEqual(
                configuredKey.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            error(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_api_key");
            return;
        }
        chain.doFilter(request, response);
    }

    private void error(HttpServletResponse response, int status, String code) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"error\":\"" + code + "\"}");
    }
}
