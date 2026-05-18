package com.houyu.gateway.service;

import com.alicp.jetcache.Cache;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.CreateCache;
import com.houyu.gateway.exception.GatewayException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {

    @CreateCache(name = "gateway:permissions:",
                 cacheType = CacheType.REMOTE,
                 expire = 300,
                 timeUnit = TimeUnit.SECONDS)
    private Cache<String, Set<String>> permissionCache;

    private final PermissionClient permissionClient;

    public AuthService(PermissionClient permissionClient) {
        this.permissionClient = permissionClient;
    }

    public void checkPermission(String userId, String uri, String method) {
        if (userId == null || userId.isEmpty()) {
            throw new GatewayException("UNAUTHORIZED", "User not authenticated");
        }

        Set<String> permissions = getPermissions(userId);
        
        String requiredPermission = buildPermissionKey(uri, method);
        
        if (!permissions.contains(requiredPermission)) {
            throw new GatewayException("FORBIDDEN", "Insufficient permissions");
        }
    }

    private Set<String> getPermissions(String userId) {
        return permissionCache.computeIfAbsent(userId, this::loadPermissionsFromRemote);
    }

    private Set<String> loadPermissionsFromRemote(String userId) {
        return permissionClient.fetchUserPermissions(userId);
    }

    private String buildPermissionKey(String uri, String method) {
        return method.toUpperCase() + ":" + uri;
    }

    public void refreshUserPermissions(String userId) {
        permissionCache.remove(userId);
    }
}