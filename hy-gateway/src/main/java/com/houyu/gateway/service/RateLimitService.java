package com.houyu.gateway.service;

import com.houyu.gateway.config.GatewayProperties;
import com.houyu.gateway.exception.GatewayException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;
    private final GatewayProperties gatewayProperties;

    public RateLimitService(StringRedisTemplate redisTemplate, GatewayProperties gatewayProperties) {
        this.redisTemplate = redisTemplate;
        this.gatewayProperties = gatewayProperties;
    }

    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        String redisKey = "gateway:ratelimit:" + key;
        
        Long current = redisTemplate.opsForValue().increment(redisKey);
        
        if (current == 1) {
            redisTemplate.expire(redisKey, windowSeconds, TimeUnit.SECONDS);
        }
        
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