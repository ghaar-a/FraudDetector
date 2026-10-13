package com.fraud_detector.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final Pattern VALID_CORRELATION_ID =
            Pattern.compile("^[A-Za-z0-9._-]{1,100}$");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String correlationId = resolveCorrelationId(
                request.getHeader(CorrelationIdContext.HEADER_NAME)
        );

        MDC.put(CorrelationIdContext.MDC_KEY, correlationId);
        response.setHeader(
                CorrelationIdContext.HEADER_NAME,
                correlationId
        );

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CorrelationIdContext.MDC_KEY);
        }
    }

    private String resolveCorrelationId(String suppliedCorrelationId) {
        if (suppliedCorrelationId != null
                && VALID_CORRELATION_ID
                .matcher(suppliedCorrelationId)
                .matches()) {
            return suppliedCorrelationId;
        }

        return UUID.randomUUID().toString();
    }
}
