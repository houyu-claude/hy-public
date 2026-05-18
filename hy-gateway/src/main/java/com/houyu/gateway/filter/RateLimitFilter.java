package com.houyu.gateway.filter;

import com.houyu.gateway.service.RateLimitService;
import com.houyu.gateway.util.IpUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = IpUtils.getClientIp(exchange.getRequest());
        String userId = exchange.getRequest().getHeaders().getFirst(USER_ID_HEADER);
        String uri = exchange.getRequest().getPath().value();

        rateLimitService.checkRateLimit(clientIp, userId, uri);

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 3;
    }
}