package com.houyu.gateway.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class GatewayLogEvent {
    private String traceId;
    private String spanId;
    private String parentSpanId;
    private LocalDateTime timestamp;
    private String serviceName;
    private String message;
    private Map<String, String> mdcContext;
    private HttpRequestInfo httpRequest;
    private HttpResponseInfo httpResponse;
    private Long executionTime;
    private Boolean success;

    @Data
    public static class HttpRequestInfo {
        private String method;
        private String uri;
        private String queryString;
        private Map<String, String> headers;
        private String requestBody;
        private String clientIp;
    }

    @Data
    public static class HttpResponseInfo {
        private Integer statusCode;
        private String responseBody;
    }
}