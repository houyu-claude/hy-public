package com.houyu.gateway.model;

import lombok.Data;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Data
public class RouteRule {
    
    private String id;
    private String uri;
    private List<PredicateDefinition> predicates;
    private List<FilterDefinition> filters;
    private Integer order;

    @Data
    public static class PredicateDefinition {
        private String name;
        private String value;
    }

    @Data
    public static class FilterDefinition {
        private String name;
        private String value;
    }

    public RouteDefinition toRouteDefinition() {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Route id cannot be null or empty");
        }
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("Route uri cannot be null or empty");
        }

        RouteDefinition definition = new RouteDefinition();
        definition.setId(this.id);
        
        try {
            definition.setUri(URI.create(this.uri));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URI format: " + this.uri, e);
        }
        
        definition.setOrder(this.order != null ? this.order : 0);
        
        return definition;
    }
}