package com.houyu.gateway.route;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.houyu.gateway.model.RouteRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

@Component
public class DynamicRouteLocator implements RouteDefinitionLocator {

    private static final Logger logger = LoggerFactory.getLogger(DynamicRouteLocator.class);
    
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
    public void init() {
        try {
            loadRoutes();
        } catch (NacosException e) {
            logger.error("Failed to load routes from Nacos during initialization", e);
        }
        addConfigListener();
    }

    private void loadRoutes() throws NacosException {
        String config = configService.getConfig(NACOS_DATA_ID, NACOS_GROUP, 3000L);
        if (config != null && !config.isEmpty()) {
            try {
                List<RouteRule> rules = objectMapper.readValue(config, 
                        new TypeReference<List<RouteRule>>() {});
                List<RouteDefinition> newRoutes = new ArrayList<>();
                for (RouteRule rule : rules) {
                    try {
                        RouteDefinition route = rule.toRouteDefinition();
                        newRoutes.add(route);
                    } catch (Exception e) {
                        logger.error("Failed to convert route rule: {}", rule.getId(), e);
                    }
                }
                routeDefinitions.clear();
                routeDefinitions.addAll(newRoutes);
                logger.info("Successfully loaded {} routes from Nacos", routeDefinitions.size());
            } catch (Exception e) {
                logger.error("Failed to parse route configuration from Nacos", e);
            }
        }
    }

    private void addConfigListener() {
        try {
            configService.addListener(NACOS_DATA_ID, NACOS_GROUP, new Listener() {
                @Override
                public void receiveConfigInfo(String config) {
                    if (config == null || config.isEmpty()) {
                        logger.warn("Received empty route configuration from Nacos");
                        return;
                    }
                    try {
                        List<RouteRule> rules = objectMapper.readValue(config,
                                new TypeReference<List<RouteRule>>() {});
                        List<RouteDefinition> newRoutes = new ArrayList<>();
                        for (RouteRule rule : rules) {
                            try {
                                RouteDefinition route = rule.toRouteDefinition();
                                newRoutes.add(route);
                            } catch (Exception e) {
                                logger.error("Failed to convert route rule: {}", rule.getId(), e);
                            }
                        }
                        
                        if (!newRoutes.isEmpty()) {
                            routeDefinitions.clear();
                            routeDefinitions.addAll(newRoutes);
                            logger.info("Successfully updated routes from Nacos, total: {}", routeDefinitions.size());
                        } else {
                            logger.warn("No valid routes found in updated configuration, keeping previous routes");
                        }
                    } catch (Exception e) {
                        logger.error("Failed to parse updated route configuration from Nacos, keeping previous routes", e);
                    }
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });
            logger.info("Nacos config listener registered for gateway-routes");
        } catch (NacosException e) {
            logger.error("Failed to register Nacos config listener", e);
        }
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routeDefinitions);
    }
}