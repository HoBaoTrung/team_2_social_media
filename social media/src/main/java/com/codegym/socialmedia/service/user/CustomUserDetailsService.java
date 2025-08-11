package com.codegym.socialmedia.service.user;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private IUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;

        // Thử tìm bằng username trước
        if (username.contains("@")) {
            // Nếu có @ thì tìm bằng email
            user = userRepository.findByEmail(username);
        } else {
            // Ngược lại tìm bằng username
            user = userRepository.findByUsername(username);
        }

        // Nếu không tìm thấy, thử cách còn lại
        if (user == null) {
            if (username.contains("@")) {
                user = userRepository.findByUsername(username);
            } else {
                user = userRepository.findByEmail(username);
            }
        }

        if (user == null) {
            throw new UsernameNotFoundException("Không tìm thấy người dùng với tên đăng nhập hoặc email: " + username);
        }

        // Kiểm tra trạng thái tài khoản
        if (!user.isActive()) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + username);
        }

        if (user.getAccountStatus() == User.AccountStatus.BANNED) {
            throw new UsernameNotFoundException("Tài khoản đã bị cấm: " + username);
        }

        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new UsernameNotFoundException("Tài khoản đã bị tạm khóa: " + username);
        }
        return new CustomUserPrincipal(user);

    }
}