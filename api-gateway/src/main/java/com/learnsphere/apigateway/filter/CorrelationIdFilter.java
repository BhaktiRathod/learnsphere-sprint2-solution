package com.learnsphere.apigateway.filter;

import java.util.UUID;

import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

// Accepts X-Correlation-ID from the client, or generates one, and forwards it downstream.
@Component
public class CorrelationIdFilter implements WebFilter, Ordered {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        String incomingCorrelationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        String correlationId =
                (incomingCorrelationId == null || incomingCorrelationId.isBlank())
                        ? UUID.randomUUID().toString()
                        : incomingCorrelationId;

        ServerWebExchange updatedExchange = exchange.mutate()
                .request(request -> request.headers(headers ->
                        headers.set(CORRELATION_ID_HEADER, correlationId)))
                .build();

        return chain.filter(updatedExchange);
    }
}
