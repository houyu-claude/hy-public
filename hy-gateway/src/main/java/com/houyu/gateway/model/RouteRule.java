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
        
        List<PredicateDefinition> safePredicates = this.predicates != null ? this.predicates : Collections.emptyList();
        safePredicates.forEach(predicate -> {
            if (predicate != null && predicate.getName() != null && !predicate.getName().isEmpty()) {
                org.springframework.cloud.gateway.route.PredicateDefinition pd = 
                        new org.springframework.cloud.gateway.route.PredicateDefinition();
                pd.setName(predicate.getName());
                if (predicate.getValue() != null) {
                    pd.addArg("_genkey_0", predicate.getValue());
                }
                definition.getPredicates().add(pd);
            }
        });
        
        List<FilterDefinition> safeFilters = this.filters != null ? this.filters : Collections.emptyList();
        safeFilters.forEach(filter -> {
            if (filter != null && filter.getName() != null && !filter.getName().isEmpty()) {
                org.springframework.cloud.gateway.route.FilterDefinition fd = 
                        new org.springframework.cloud.gateway.route.FilterDefinition();
                fd.setName(filter.getName());
                if (filter.getValue() != null) {
                    fd.addArg("_genkey_0", filter.getValue());
                }
                definition.getFilters().add(fd);
            }
        });
        
        return definition;
    }
}