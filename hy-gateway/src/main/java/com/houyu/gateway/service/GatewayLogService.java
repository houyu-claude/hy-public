package com.houyu.gateway.service;

import com.houyu.gateway.model.GatewayLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class GatewayLogService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayLogService.class);

    public void logRequest(String clientIp, String method, String uri, String queryString,
                          Map<String, String> headers, String requestBody) {
        GatewayLogEvent logEvent = createLogEvent();
        logEvent.setMessage("Gateway incoming request");
        
        GatewayLogEvent.HttpRequestInfo httpRequest = new GatewayLogEvent.HttpRequestInfo();
        httpRequest.setMethod(method);
        httpRequest.setUri(uri);
        httpRequest.setQueryString(queryString);
        httpRequest.setHeaders(headers);
        httpRequest.setRequestBody(requestBody);
        httpRequest.setClientIp(clientIp);
        
        logEvent.setHttpRequest(httpRequest);
        
        logger.info("Gateway request - {} {} from {}", method, uri, clientIp);
    }

    public void logResponse(int statusCode, String responseBody, long executionTime) {
        GatewayLogEvent logEvent = createLogEvent();
        logEvent.setMessage("Gateway response");
        logEvent.setExecutionTime(executionTime);
        logEvent.setSuccess(statusCode >= 200 && statusCode < 400);
        
        GatewayLogEvent.HttpResponseInfo httpResponse = new GatewayLogEvent.HttpResponseInfo();
        httpResponse.setStatusCode(statusCode);
        httpResponse.setResponseBody(responseBody);
        
        logEvent.setHttpResponse(httpResponse);
        
        logger.info("Gateway response - status: {}, time: {}ms", statusCode, executionTime);
    }

    private GatewayLogEvent createLogEvent() {
        GatewayLogEvent logEvent = new GatewayLogEvent();
        logEvent.setTimestamp(LocalDateTime.now());
        logEvent.setServiceName("hy-gateway");
        logEvent.setMdcContext(new HashMap<>());
        
        return logEvent;
    }
}