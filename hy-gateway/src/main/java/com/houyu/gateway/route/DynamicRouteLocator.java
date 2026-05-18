package com.houyu.gateway.route;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houyu.gateway.model.RouteRule;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

@Component
public class DynamicRouteLocator implements RouteDefinitionLocator {

    private static final String NACOS_DATA_ID = "gateway-routes";
    private static final String NACOS_GROUP = "DEFAULT_GROUP";

    private final ConfigService configService;
    private final ObjectMapper objectMapper;
    private final List<RouteDefinition> routeDefinitions = new CopyOnWriteArrayList<>();

    public DynamicRouteLocator(ConfigService configService, ObjectMapper objectMapper) {
        this.configService = configService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() throws NacosException {
        loadRoutes();
        addConfigListener();
    }

    private void loadRoutes() throws NacosException {
        String config = configService.getConfig(NACOS_DATA_ID, NACOS_GROUP, 3000L);
        if (config != null && !config.isEmpty()) {
            List<RouteRule> rules = objectMapper.readValue(config, 
                    new TypeReference<List<RouteRule>>() {});
            routeDefinitions.clear();
            rules.forEach(rule -> routeDefinitions.add(rule.toRouteDefinition()));
        }
    }

    private void addConfigListener() {
        try {
            configService.addListener(NACOS_DATA_ID, NACOS_GROUP, new Listener() {
                @Override
                public void receiveConfigInfo(String config) {
                    try {
                        List<RouteRule> rules = objectMapper.readValue(config,
                                new TypeReference<List<RouteRule>>() {});
                        routeDefinitions.clear();
                        rules.forEach(rule -> routeDefinitions.add(rule.toRouteDefinition()));
                    } catch (Exception e) {
                        // log error
                    }
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
        } catch (NacosException e) {
            // log error
        }
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routeDefinitions);
    }
}