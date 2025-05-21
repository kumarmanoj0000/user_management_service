package com.example.usermanagement.security.oauth2;

import com.example.usermanagement.security.oauth2.user.OAuth2UserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface CustomOAuth2UserService {
    OAuth2UserInfo processOAuth2User(String registrationId, OAuth2User oAuth2User);
}
