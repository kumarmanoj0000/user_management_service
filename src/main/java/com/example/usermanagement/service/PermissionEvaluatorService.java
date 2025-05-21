package com.example.usermanagement.service;

import org.springframework.stereotype.Service;

@Service("permissionEvaluatorService")
public class PermissionEvaluatorService {

    public String getPrefixedPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission string cannot be null or empty.");
        }
        return "PERMISSION_" + permission.toUpperCase();
    }
}
