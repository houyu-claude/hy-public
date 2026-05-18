package com.houyu.gateway.filter;

import com.houyu.gateway.config.WhitelistProperties;
import com.houyu.gateway.exception.GatewayException;
import com.houyu.gateway.service.TokenService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TokenFilter implements GlobalFilter, Ordered {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenService tokenService;
    private final WhitelistProperties whitelistProperties;

    public TokenFilter(TokenService tokenService, WhitelistProperties whitelistProperties) {
        this.tokenService = tokenService;
        this.whitelistProperties = whitelistProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        
        if (whitelistProperties.isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String token = exchange.getRequest().getHeaders().getFirst(AUTHORIZATION_HEADER);
        
        if (token == null || token.isEmpty()) {
            throw new GatewayException("TOKEN_MISSING", "Token is missing");
        }

        String userId = tokenService.getUserIdFromToken(token);
        String username = tokenService.getUsernameFromToken(token);
        String tenantId = tokenService.getTenantIdFromToken(token);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.set("X-User-Id", userId);
                    headers.set("X-Username", username);
                    headers.set("X-Tenant-Id", tenantId);
                }))
                .build();

        return chain.filter(mutatedExchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}