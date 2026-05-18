package com.houyu.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    private Key signingKey;

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

    public void setSigningKey(Key signingKey) {
        this.signingKey = signingKey;
    }
}