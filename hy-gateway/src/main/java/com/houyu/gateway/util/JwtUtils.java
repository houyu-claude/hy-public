package com.houyu.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    
    private static final int MIN_SECRET_LENGTH = 32;

    @Value("${hy.gateway.jwt.secret:houyu-gateway-secret-key-must-be-at-least-32-characters-long-for-security}")
    private String jwtSecret;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (jwtSecret.length() < MIN_SECRET_LENGTH) {
            logger.warn("JWT secret key is less than {} characters, consider using a longer key for better security", MIN_SECRET_LENGTH);
        }
        
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        
        if (keyBytes.length < 32) {
            byte[] paddedKey = new byte[32];
            System.arraycopy(keyBytes, 0, paddedKey, 0, keyBytes.length);
            keyBytes = paddedKey;
        }
        
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Map<String, Object> convertClaimsToMap(Claims claims) {
        Map<String, Object> map = new HashMap<>();
        claims.forEach(map::put);
        return map;
    }
}