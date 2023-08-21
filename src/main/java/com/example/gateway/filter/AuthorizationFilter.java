package com.example.gateway.filter;

import com.example.gateway.jwt.JwtValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthorizationFilter extends AbstractGatewayFilterFactory<AuthorizationFilter.Config> {

    private final JwtValidator jwtValidator;

    public AuthorizationFilter(JwtValidator jwtValidator) {
        super(AuthorizationFilter.Config.class);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                throw new IllegalStateException("BAD REQUEST");
            }

            String parsedToken = jwtValidator.getToken(request);
            String ExtractedToken = jwtValidator.extractToken(parsedToken);

            jwtValidator.validateToken(ExtractedToken);

            Long memberId = jwtValidator.getId(ExtractedToken);
            log.info("memberId = {}", memberId);

            updateHeadersWithAuthorizationId(exchange.getRequest(), memberId);

            return chain.filter(exchange);
        });
    }

    private void updateHeadersWithAuthorizationId(ServerHttpRequest request, Long memberId) {
        request.mutate()
                .headers(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                .header("X-Authorization-Id", memberId.toString())
                .build();
    }

    public static class Config {
    }
}

