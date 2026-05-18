package com.houyu.gateway.service;

import com.houyu.gateway.config.GatewayProperties;
import com.houyu.gateway.exception.GatewayException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final GatewayProperties gatewayProperties;

    private static final String RATE_LIMIT_LUA_SCRIPT = """
            local key = KEYS[1]
            local limit = tonumber(ARGV[1])
            local windowSeconds = tonumber(ARGV[2])
            
            local current = redis.call('INCR', key)
            if current == 1 then
                redis.call('EXPIRE', key, windowSeconds)
            end
            
            return current
            """;

    private final DefaultRedisScript<Long> rateLimitScript;

    public RateLimitService(StringRedisTemplate redisTemplate, GatewayProperties gatewayProperties) {
        this.redisTemplate = redisTemplate;
        this.gatewayProperties = gatewayProperties;
        this.rateLimitScript = new DefaultRedisScript<>();
        this.rateLimitScript.setScriptText(RATE_LIMIT_LUA_SCRIPT);
        this.rateLimitScript.setResultType(Long.class);
    }

    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        String redisKey = "gateway:ratelimit:" + key;
        List<String> keys = Collections.singletonList(redisKey);
        
        Long current = redisTemplate.execute(rateLimitScript, keys, String.valueOf(limit), String.valueOf(windowSeconds));
        
        return current != null && current <= limit;
    }

    public void checkRateLimit(String clientIp, String userId, String uri) {
        GatewayProperties.RateLimit rateLimit = gatewayProperties.getRateLimit();
        
        String ipKey = "ip:" + clientIp;
        String userKey = userId != null ? "user:" + userId : null;
        String uriKey = "uri:" + uri;

        if (!tryAcquire(ipKey, rateLimit.getIpLimit(), 60)) {
            throw new GatewayException("RATE_LIMIT_IP", "IP rate limit exceeded");
        }

        if (userKey != null && !tryAcquire(userKey, rateLimit.getUserLimit(), 60)) {
            throw new GatewayException("RATE_LIMIT_USER", "User rate limit exceeded");
        }

        if (!tryAcquire(uriKey, rateLimit.getUriLimit(), 60)) {
            throw new GatewayException("RATE_LIMIT_URI", "URI rate limit exceeded");
        }
    }
}