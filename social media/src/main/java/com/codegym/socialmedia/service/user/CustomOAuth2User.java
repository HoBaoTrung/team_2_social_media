package com.codegym.socialmedia.service.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collection;
import java.util.Map;
public class CustomOAuth2User extends DefaultOAuth2User {

    private String avatarUrl;

    public CustomOAuth2User(Collection<? extends GrantedAuthority> authorities,
                            Map<String, Object> attributes,
                            String nameAttributeKey,
                            String avatarUrl) {
        super(authorities, attributes, nameAttributeKey);
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