package com.houyu.gateway.model;

import lombok.Data;
import org.springframework.cloud.gateway.route.RouteDefinition;

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
        RouteDefinition definition = new RouteDefinition();
        definition.setId(this.id);
        definition.setUri(java.net.URI.create(this.uri));
        definition.setOrder(this.order != null ? this.order : 0);
        
        this.predicates.forEach(predicate -> {
            org.springframework.cloud.gateway.route.PredicateDefinition pd = 
                    new org.springframework.cloud.gateway.route.PredicateDefinition();
            pd.setName(predicate.getName());
            pd.addArg("_genkey_0", predicate.getValue());
            definition.getPredicates().add(pd);
        });
        
        this.filters.forEach(filter -> {
            org.springframework.cloud.gateway.route.FilterDefinition fd = 
                    new org.springframework.cloud.gateway.route.FilterDefinition();
            fd.setName(filter.getName());
            fd.addArg("_genkey_0", filter.getValue());
            definition.getFilters().add(fd);
        });
        
        return definition;
    }
}