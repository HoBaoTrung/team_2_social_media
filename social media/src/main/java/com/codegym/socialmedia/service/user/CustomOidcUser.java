package com.codegym.socialmedia.service.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.util.Collection;
import java.util.Map;

public class CustomOidcUser extends DefaultOidcUser {

    private final String avatarUrl;

    public CustomOidcUser(Collection<? extends GrantedAuthority> authorities,
                          OidcIdToken idToken,
                          OidcUserInfo userInfo,
                          String nameAttributeKey,
                          Map<String, Object> attributes,
                          String avatarUrl) {
        super(authorities, idToken, userInfo, nameAttributeKey);
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getEmail() {
        return (String) getAttributes().get("email");
    }

    public String getName() {
        return (String) getAttributes().get("name");
    }
}
