package com.example.usermanagement.security.oauth2.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OAuth2UserInfo {
    private String providerId; // Unique ID from the provider
    private String provider;   // Name of the provider (e.g., "google", "github")
    private String email;
    private String name;
    private String imageUrl;
}
