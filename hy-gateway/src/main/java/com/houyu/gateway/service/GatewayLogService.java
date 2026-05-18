package com.houyu.gateway.service;

import com.houyu.common.log.model.HyLogEvent;
import com.houyu.common.log.model.HttpRequestInfo;
import com.houyu.common.log.model.HttpResponseInfo;
import com.houyu.common.log.output.LogOutputManager;
import com.houyu.common.log.trace.TraceContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class GatewayLogService {

    private static final Logger logger = LoggerFactory.getLogger(GatewayLogService.class);

    private final LogOutputManager logOutputManager;

    public GatewayLogService(LogOutputManager logOutputManager) {
        this.logOutputManager = logOutputManager;
    }

    public void logRequest(String clientIp, String method, String uri, String queryString,
                          Map<String, String> headers, String requestBody) {
        HyLogEvent logEvent = createLogEvent();
        logEvent.setMessage("Gateway incoming request");
        
        HttpRequestInfo httpRequest = new HttpRequestInfo();
        httpRequest.setMethod(method);
        httpRequest.setUri(uri);
        httpRequest.setQueryString(queryString);
        httpRequest.setHeaders(headers);
        httpRequest.setRequestBody(requestBody);
        httpRequest.setClientIp(clientIp);
        
        logEvent.setHttpRequest(httpRequest);
        
        logOutputManager.output(logEvent);
        
        logger.info("Gateway request - {} {} from {}", method, uri, clientIp);
    }

    public void logResponse(int statusCode, String responseBody, long executionTime) {
        HyLogEvent logEvent = createLogEvent();
        logEvent.setMessage("Gateway response");
        logEvent.setExecutionTime(executionTime);
        logEvent.setSuccess(statusCode >= 200 && statusCode < 400);
        
        HttpResponseInfo httpResponse = new HttpResponseInfo();
        httpResponse.setStatusCode(statusCode);
        httpResponse.setResponseBody(responseBody);
        
        logEvent.setHttpResponse(httpResponse);
        
        logOutputManager.output(logEvent);
        
        logger.info("Gateway response - status: {}, time: {}ms", statusCode, executionTime);
    }

    private HyLogEvent createLogEvent() {
        HyLogEvent logEvent = new HyLogEvent();
        logEvent.setTraceId(TraceContextHolder.getTraceId());
        logEvent.setSpanId(TraceContextHolder.getSpanId());
        logEvent.setParentSpanId(TraceContextHolder.getParentSpanId());
        logEvent.setTimestamp(LocalDateTime.now());
        logEvent.setServiceName("hy-gateway");
        logEvent.setMdcContext(new HashMap<>());
        
        return logEvent;
    }
}