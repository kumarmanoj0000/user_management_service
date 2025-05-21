package com.example.usermanagement.controller;

import com.example.usermanagement.dto.UserDto;
import com.example.usermanagement.security.annotation.HasPermission;
import com.example.usermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ROLE_ADMIN')") // Class-level security for admin role
public class AdminController {

    @Autowired
    private UserService userService;

    @GetMapping
    @HasPermission("ADMIN_USER_READ_ALL")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @HasPermission("ADMIN_USER_READ_ONE")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/roles")
    @HasPermission("ADMIN_USER_UPDATE_ROLES")
    public ResponseEntity<UserDto> updateUserRoles(@PathVariable Long id, @RequestBody Set<String> roleNames) {
        try {
            UserDto updatedUser = userService.updateUserRolesAndPermissions(id, roleNames);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) { // Catch specific exceptions like EntityNotFoundException if preferred
            return ResponseEntity.badRequest().build(); // Or more specific error
        }
    }

    @DeleteMapping("/{id}")
    @HasPermission("ADMIN_USER_DELETE")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) { // Catch specific exceptions
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/enable")
    @HasPermission("ADMIN_USER_UPDATE_STATUS")
    public ResponseEntity<UserDto> enableUser(@PathVariable Long id, @RequestParam boolean enable) {
        try {
            UserDto updatedUser = userService.enableUser(id, enable);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) { // Catch specific exceptions
            return ResponseEntity.badRequest().build();
        }
    }
}
