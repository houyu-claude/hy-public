package com.houyu.gateway.filter;

import com.houyu.gateway.config.WhitelistProperties;
import com.houyu.gateway.service.AuthService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final AuthService authService;
    private final WhitelistProperties whitelistProperties;

    public AuthFilter(AuthService authService, WhitelistProperties whitelistProperties) {
        this.authService = authService;
        this.whitelistProperties = whitelistProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        String userId = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER);
        String uri = path;
        String method = exchange.getRequest().getMethod().name();

        try {
            authService.checkPermission(userId, uri, method);
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean isWhitelisted(String path) {
        List<String> whitelist = whitelistProperties.getPaths();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        return whitelist.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}