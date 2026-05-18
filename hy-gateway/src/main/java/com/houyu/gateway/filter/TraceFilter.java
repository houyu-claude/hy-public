package com.houyu.gateway.filter;

import com.houyu.common.log.trace.TraceContextHolder;
import com.houyu.common.log.trace.TraceIdGenerator;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TraceFilter implements GlobalFilter, Ordered {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String TRACE_FLAG_HEADER = "X-Trace-Flag";

    private final TraceIdGenerator traceIdGenerator;

    public TraceFilter(TraceIdGenerator traceIdGenerator) {
        this.traceIdGenerator = traceIdGenerator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String incomingTraceId = exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER);
        String incomingSpanId = exchange.getRequest().getHeaders().getFirst(SPAN_ID_HEADER);
        String traceFlag = exchange.getRequest().getHeaders().getFirst(TRACE_FLAG_HEADER);

        String traceId = (incomingTraceId != null && !incomingTraceId.isEmpty())
                ? incomingTraceId
                : traceIdGenerator.generateTraceId(traceFlag);
        
        String spanId = traceIdGenerator.generateSpanId();

        TraceContextHolder.setTraceId(traceId);
        TraceContextHolder.setSpanId(spanId);
        TraceContextHolder.setParentSpanId(incomingSpanId);

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.headers(headers -> {
                    headers.set(TRACE_ID_HEADER, traceId);
                    headers.set(SPAN_ID_HEADER, spanId);
                }))
                .build();

        return chain.filter(mutatedExchange).doFinally(signalType -> {
            TraceContextHolder.clear();
        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}