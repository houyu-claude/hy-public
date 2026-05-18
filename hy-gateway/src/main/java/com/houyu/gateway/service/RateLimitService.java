package com.houyu.gateway.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.houyu.gateway.exception.GatewayException;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    @CreateCache(name = "gateway:ratelimit:",
                 cacheType = CacheType.REMOTE,
                 expire = 60,
                 timeUnit = TimeUnit.SECONDS)
    private Cache<String, AtomicInteger> rateLimitCache;

    public boolean tryAcquire(String key, int limit, int windowSeconds) {
        AtomicInteger counter = rateLimitCache.computeIfAbsent(key, k -> new AtomicInteger(0));
        
        int current = counter.incrementAndGet();
        
        if (current == 1) {
            rateLimitCache.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        
        return current <= limit;
    }

    public void checkRateLimit(String clientIp, String userId, String uri) {
        String ipKey = "ip:" + clientIp;
        String userKey = userId != null ? "user:" + userId : null;
        String uriKey = "uri:" + uri;

        if (!tryAcquire(ipKey, 100, 60)) {
            throw new GatewayException("RATE_LIMIT_IP", "IP rate limit exceeded");
        }

        if (userKey != null && !tryAcquire(userKey, 50, 60)) {
            throw new GatewayException("RATE_LIMIT_USER", "User rate limit exceeded");
        }

        if (!tryAcquire(uriKey, 500, 60)) {
            throw new GatewayException("RATE_LIMIT_URI", "URI rate limit exceeded");
        }
    }
}