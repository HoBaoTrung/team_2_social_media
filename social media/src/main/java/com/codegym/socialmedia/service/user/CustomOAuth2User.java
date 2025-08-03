package com.codegym.socialmedia.service.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

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

//public class CustomOAuth2User implements OAuth2User {
//
//    private final OAuth2User oauth2User;
//
//    public CustomOAuth2User(OAuth2User oauth2User) {
//        this.oauth2User = oauth2User;
//    }
//
//    @Override
//    public Map<String, Object> getAttributes() {
//        return oauth2User.getAttributes();
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return oauth2User.getAuthorities();
//    }
//
//    @Override
//    public String getName() {
//        return oauth2User.getAttribute("name"); // hoặc "sub"
//    }
//
//    public String getEmail() {
//        return oauth2User.getAttribute("email");
//    }
//
//    //    public String getAvatarUrl() {
////        return oauth2User.getAttribute("picture");
////    }
//    public String getAvatarUrl() {
//        Object picture = oauth2User.getAttribute("picture");
//        if (picture instanceof Map) {
//            Object data = ((Map<?, ?>) picture).get("data");
//            if (data instanceof Map) {
//                return (String) ((Map<?, ?>) data).get("url");
//            }
//        }
//        return null;
//    }
//
//}
