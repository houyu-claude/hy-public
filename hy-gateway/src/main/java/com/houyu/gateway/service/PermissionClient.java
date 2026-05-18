package com.houyu.gateway.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

@FeignClient(name = "hy-auth")
public interface PermissionClient {

    @GetMapping("/api/permission/user")
    Set<String> fetchUserPermissions(@RequestParam("userId") String userId);
}