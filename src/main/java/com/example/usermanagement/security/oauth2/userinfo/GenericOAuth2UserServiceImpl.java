package com.example.usermanagement.security.oauth2.userinfo;

import com.example.usermanagement.security.oauth2.CustomOAuth2UserService;
import com.example.usermanagement.security.oauth2.user.OAuth2UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GenericOAuth2UserServiceImpl implements CustomOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(GenericOAuth2UserServiceImpl.class);

    @Override
    public OAuth2UserInfo processOAuth2User(String registrationId, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.info("Processing OAuth2 user for registrationId: {}", registrationId);
        logger.info("User attributes: {}", attributes);

        OAuth2UserInfo.OAuth2UserInfoBuilder builder = OAuth2UserInfo.builder();
        builder.provider(registrationId);

        // Common attribute extraction logic (examples)
        if ("google".equalsIgnoreCase(registrationId)) {
            builder.providerId(attributes.getOrDefault("sub", "").toString());
            builder.email(attributes.getOrDefault("email", "").toString());
            builder.name(attributes.getOrDefault("name", "").toString());
            builder.imageUrl(attributes.getOrDefault("picture", "").toString());
        } else if ("github".equalsIgnoreCase(registrationId)) {
            builder.providerId(attributes.getOrDefault("id", "").toString());
            builder.email(attributes.getOrDefault("email", "").toString()); // May be null if not public
            // GitHub might not provide 'name' directly, 'login' is username.
            // 'name' might be in a different field or null.
            builder.name(attributes.getOrDefault("name", attributes.getOrDefault("login", "")).toString());
            builder.imageUrl(attributes.getOrDefault("avatar_url", "").toString());
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
             builder.providerId(attributes.getOrDefault("id", "").toString());
             builder.email(attributes.getOrDefault("email", "").toString());
             builder.name(attributes.getOrDefault("name", "").toString());
             if (attributes.containsKey("picture")) {
                 Map<String, Object> pictureObj = (Map<String, Object>) attributes.get("picture");
                 if (pictureObj.containsKey("data")) {
                     Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
                     builder.imageUrl(dataObj.getOrDefault("url", "").toString());
                 }
             }
        }
        // Add more providers as needed

        OAuth2UserInfo userInfo = builder.build();
        logger.info("Processed OAuth2UserInfo: {}", userInfo);
        return userInfo;
    }
}
