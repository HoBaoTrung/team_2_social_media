package com.codegym.socialmedia.service.admin;

import com.codegym.socialmedia.model.admin.Admin;
import com.codegym.socialmedia.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class AdminDetailsService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));

        return new org.springframework.security.core.userdetails.User(
                admin.getUsername(),
                admin.getPasswordHash(),
                getAuthorities(admin)
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Admin admin) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + admin.getRole().getName()));
    }
}
