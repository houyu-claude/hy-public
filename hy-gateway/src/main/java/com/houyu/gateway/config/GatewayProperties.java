package com.houyu.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "hy.gateway")
public class GatewayProperties {
    private RateLimit rateLimit = new RateLimit();

    @Data
    public static class RateLimit {
        private int ipLimit = 100;
        private int userLimit = 50;
        private int uriLimit = 500;
    }
}