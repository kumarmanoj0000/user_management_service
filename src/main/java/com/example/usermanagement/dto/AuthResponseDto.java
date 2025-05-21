package com.example.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String message;
    private boolean success;
    private String token; // Added for future login response

    public AuthResponseDto(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
}
