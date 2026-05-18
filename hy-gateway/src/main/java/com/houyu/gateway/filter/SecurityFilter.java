package com.houyu.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Component
public class SecurityFilter implements GlobalFilter, Ordered {

    private static final Pattern XSS_PATTERN = Pattern.compile(
            "<script[^>]*>.*?</script>|javascript:|onload=|onclick=|onerror=|eval\\(",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            "(?i)(select|insert|update|delete|drop|union|exec|execute|xp_)",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String queryString = exchange.getRequest().getQueryParams().toString();
        String path = exchange.getRequest().getPath().value();

        if (containsXss(queryString) || containsXss(path)) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        if (containsSqlInjection(queryString) || containsSqlInjection(path)) {
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private boolean containsXss(String input) {
        return input != null && XSS_PATTERN.matcher(input).find();
    }

    private boolean containsSqlInjection(String input) {
        return input != null && SQL_INJECTION_PATTERN.matcher(input).find();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 4;
    }
}