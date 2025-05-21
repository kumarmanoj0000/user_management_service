package com.example.usermanagement.security.annotation;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(@permissionEvaluatorService.getPrefixedPermission(#permission))")
public @interface HasPermission {
    String permission(); // Changed from value() to permission() to match the @PreAuthorize expression
}
