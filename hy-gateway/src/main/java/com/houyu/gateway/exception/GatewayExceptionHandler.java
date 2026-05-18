package com.houyu.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GatewayExceptionHandler {

    @ExceptionHandler(GatewayException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGatewayException(GatewayException e) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", e.getCode());
        response.put("message", e.getMessage());
        
        HttpStatus status = mapCodeToStatus(e.getCode());
        
        return Mono.just(ResponseEntity.status(status).body(response));
    }

    private HttpStatus mapCodeToStatus(String code) {
        return switch (code) {
            case "TOKEN_MISSING", "TOKEN_INVALID", "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "FORBIDDEN" -> HttpStatus.FORBIDDEN;
            case "RATE_LIMIT_IP", "RATE_LIMIT_USER", "RATE_LIMIT_URI" -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}