package com.houyu.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "hy.gateway.whitelist")
public class WhitelistProperties {
    private List<String> paths = new ArrayList<>();
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    public boolean isWhitelisted(String path) {
        if (paths == null || paths.isEmpty()) {
            return false;
        }
        return paths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}