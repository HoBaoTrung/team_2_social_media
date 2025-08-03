package com.codegym.socialmedia.service.user;

import com.codegym.socialmedia.model.account.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    @Lazy // Thêm @Lazy để tránh circular dependency
    private UserService userService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oauth2User.getAttributes();

            String email = null;
            String name = null;

            if ("google".equals(registrationId)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");

                // Log để debug
                System.out.println("Google OAuth2 - Email: " + email + ", Name: " + name);

            } else if ("facebook".equals(registrationId)) {
                email = (String) attributes.get("email");
                name = (String) attributes.get("name");

                // Log để debug
                System.out.println("Facebook OAuth2 - Email: " + email + ", Name: " + name);
            }

            if (email != null && name != null) {
                User user = userService.createOrUpdateOAuth2User(email, name, registrationId);
                System.out.println("OAuth2 User created/updated: " + user.getUsername() + " - " + user.getEmail());
            } else {
                System.out.println("OAuth2 - Missing email or name information");
            }

        } catch (Exception e) {
            System.err.println("Error processing OAuth2 user: " + e.getMessage());
            e.printStackTrace();
            throw new OAuth2AuthenticationException("Failed to process OAuth2 user");
        }

        return oauth2User;
    }
}