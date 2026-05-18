package com.houyu.gateway.filter;

import com.houyu.gateway.config.WhitelistProperties;
import com.houyu.gateway.exception.GatewayException;
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
        } catch (GatewayException e) {
            HttpStatus status = mapCodeToStatus(e.getCode());
            exchange.getResponse().setStatusCode(status);
            return exchange.getResponse().setComplete();
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
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

    private HttpStatus mapCodeToStatus(String code) {
        return switch (code) {
            case "TOKEN_MISSING", "TOKEN_INVALID", "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "RATE_LIMIT_IP", "RATE_LIMIT_USER", "RATE_LIMIT_URI" -> HttpStatus.TOO_MANY_REQUESTS;
            case "SERVICE_UNAVAILABLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 2;
    }
}