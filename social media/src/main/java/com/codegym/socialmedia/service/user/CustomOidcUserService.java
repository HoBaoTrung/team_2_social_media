package com.codegym.socialmedia.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOidcUserService extends OidcUserService
        implements OAuth2UserService<OidcUserRequest, OidcUser> {

    @Autowired
    @Lazy
    private UserService userService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        try {
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            Map<String, Object> attributes = oidcUser.getAttributes();

            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String avatarUrl = (String) attributes.get("picture");

            userService.createOrUpdateOAuth2User(email, name, registrationId, avatarUrl);

            return new CustomOidcUser(
                    oidcUser.getAuthorities(),
                    oidcUser.getIdToken(),
                    oidcUser.getUserInfo(),
                    "sub",             // key mặc định của OIDC (Google dùng "sub")
                    attributes,
                    avatarUrl
            );

        } catch (Exception e) {
            throw new OAuth2AuthenticationException("Failed to process OIDC user");
        }
    }

}
