package com.houyu.gateway.filter;

import com.houyu.gateway.service.GatewayLogService;
import com.houyu.gateway.util.IpUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class RequestLogFilter implements GlobalFilter, Ordered {

    private final GatewayLogService gatewayLogService;

    public RequestLogFilter(GatewayLogService gatewayLogService) {
        this.gatewayLogService = gatewayLogService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = IpUtils.getClientIp(exchange.getRequest());
        String method = exchange.getRequest().getMethod().name();
        String uri = exchange.getRequest().getPath().value();
        String queryString = exchange.getRequest().getQueryParams().toString();
        
        Map<String, String> headers = new HashMap<>();
        exchange.getRequest().getHeaders().forEach((key, value) -> {
            headers.put(key, value.get(0));
        });

        gatewayLogService.logRequest(clientIp, method, uri, queryString, headers, null);

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange).doFinally(signalType -> {
            long executionTime = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null 
                    ? exchange.getResponse().getStatusCode().value() 
                    : 500;
            gatewayLogService.logResponse(statusCode, null, executionTime);
        });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}