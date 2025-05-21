package com.example.usermanagement.security.oauth2;

import com.example.usermanagement.model.AuthProvider;
import com.example.usermanagement.model.Role;
import com.example.usermanagement.model.User;
import com.example.usermanagement.repository.RoleRepository;
import com.example.usermanagement.repository.UserRepository;
import com.example.usermanagement.security.jwt.JwtTokenProvider;
import com.example.usermanagement.security.oauth2.user.OAuth2UserInfo;
import com.example.usermanagement.service.UserDetailsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2AuthenticationSuccessHandler.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;
    
    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl; // To load UserDetails for JWT

    @Autowired
    private PasswordEncoder passwordEncoder; // To encode a dummy password

    // Configure the target URL for redirection (e.g., your frontend application)
    private String targetUrl = "/"; // Default, can be configured

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("OAuth2 authentication successful. Processing user...");

        OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauth2Token.getPrincipal();
        String registrationId = oauth2Token.getAuthorizedClientRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = customOAuth2UserService.processOAuth2User(registrationId, oauth2User);

        User user = provisionUser(oAuth2UserInfo, registrationId);
        
        // Generate JWT for the provisioned/retrieved local user
        // We need Spring Security's UserDetails object for this user
        UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(user.getUsername());
        
        // Create a new Authentication object based on our local UserDetails for JWT generation
        Authentication localAuthentication = new UsernamePasswordAuthenticationToken(
                userDetails, 
                null, // Credentials are not needed here
                userDetails.getAuthorities()
        );
        
        String jwt = jwtTokenProvider.generateToken(localAuthentication);
        logger.info("Generated JWT for OAuth2 user: {}", user.getUsername());

        String finalRedirectUrl = UriComponentsBuilder.fromUriString(determineTargetUrl(request, response, authentication))
                .queryParam("token", jwt)
                .build().toUriString();

        logger.info("Redirecting to: {}", finalRedirectUrl);
        getRedirectStrategy().sendRedirect(request, response, finalRedirectUrl);
        clearAuthenticationAttributes(request);
    }

    private User provisionUser(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update existing user details if necessary
            user.setName(oAuth2UserInfo.getName()); // Assuming User entity has a name field
            // user.setImageUrl(oAuth2UserInfo.getImageUrl()); // Assuming User entity has imageUrl
            if (user.getProvider() == null || !user.getProvider().toString().equalsIgnoreCase(registrationId)) {
                // If user exists but logged in with a different provider, this might be an issue or a merge case.
                // For now, we can update provider info if it's missing.
                // Or throw an exception if email is associated with a different provider.
                 logger.warn("User with email {} already exists with provider {}. Updating to new provider {}.", 
                    user.getEmail(), user.getProvider(), registrationId);
                user.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
                user.setProviderId(oAuth2UserInfo.getProviderId());
            }
        } else {
            // Create new user
            user = new User();
            user.setEmail(oAuth2UserInfo.getEmail());
            // Username: try to use email prefix or a generated unique name if email is not unique for username
            String username = oAuth2UserInfo.getEmail().split("@")[0]; 
            if (userRepository.existsByUsername(username)) {
                 username = username + "_" + UUID.randomUUID().toString().substring(0, 4);
            }
            user.setUsername(username); 
            user.setName(oAuth2UserInfo.getName()); // Assuming User entity has a name field
            // user.setImageUrl(oAuth2UserInfo.getImageUrl());

            user.setProvider(AuthProvider.valueOf(registrationId.toUpperCase()));
            user.setProviderId(oAuth2UserInfo.getProviderId());
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Dummy password

            // Assign default role
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new IllegalStateException("Error: Default role ROLE_USER not found."));
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);

            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);
            
            logger.info("Creating new user from OAuth2 info: {}", user.getEmail());
        }
        return userRepository.save(user);
    }
    
    // Optional: Allow target URL to be configured
    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (targetUrl.contains("{registrationId}")) {
            String registrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
            return targetUrl.replace("{registrationId}", registrationId);
        }
        return targetUrl;
    }
}
