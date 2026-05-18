package com.houyu.gateway.service;

import com.houyu.gateway.exception.GatewayException;
import com.houyu.gateway.util.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TokenService {

    private final JwtUtils jwtUtils;

    public TokenService(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public Map<String, Object> validateAndParseToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new GatewayException("TOKEN_MISSING", "Token is missing");
        }

        String jwtToken = extractJwtToken(token);
        
        try {
            Claims claims = jwtUtils.parseToken(jwtToken);
            return jwtUtils.convertClaimsToMap(claims);
        } catch (Exception e) {
            throw new GatewayException("TOKEN_INVALID", "Invalid token: " + e.getMessage());
        }
    }

    private String extractJwtToken(String token) {
        if (token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    public String getUserIdFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return claims.getOrDefault("userId", "").toString();
    }

    public String getUsernameFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return claims.getOrDefault("username", "").toString();
    }

    public String getTenantIdFromToken(String token) {
        Map<String, Object> claims = validateAndParseToken(token);
        return claims.getOrDefault("tenantId", "").toString();
    }
}