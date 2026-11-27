package com.learnsphere.courseservice.filter;

import java.io.IOException;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Puts the incoming X-Correlation-ID into the logging MDC for the whole request.
@Component
public class CorrelationIdMdcFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER =
            "X-Correlation-ID";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String correlationId =
                request.getHeader(CORRELATION_ID_HEADER);

        try {
            if (correlationId != null && !correlationId.isBlank()) {
                MDC.put("correlationId", correlationId);
            }

            filterChain.doFilter(request, response);

        } finally {
            MDC.remove("correlationId");
        }
    }
}
