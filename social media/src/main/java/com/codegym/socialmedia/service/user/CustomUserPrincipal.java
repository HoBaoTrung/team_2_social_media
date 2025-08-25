package com.codegym.socialmedia.service.user;

import com.codegym.socialmedia.general_interface.UserPrincipalInfo;
import com.codegym.socialmedia.model.account.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserPrincipal implements UserDetails, UserPrincipalInfo {
    private final User user;

    public CustomUserPrincipal(User user) {
        this.user = user;
    }

    public long getId(){
        return user.getId();
    }

    @Override
    public String getAvatarUrl() {
        return user.getProfilePicture();
    }

    @Override
    public String getFullName() {
        return user.getFirstName() + " " + user.getLastName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.getAccountStatus() == User.AccountStatus.ACTIVE;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }

    public User getUser() {
        return user;
    }
}
