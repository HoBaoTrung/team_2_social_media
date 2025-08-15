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
        System.out.println("🔍 Attempting to load user: " + username);

        User user = null;

        // Thử tìm bằng username trước
        if (username.contains("@")) {
            System.out.println("📧 Searching by email: " + username);
            user = userRepository.findByEmail(username);
        } else {
            System.out.println("👤 Searching by username: " + username);
            user = userRepository.findByUsername(username);
        }

        // Nếu không tìm thấy, thử cách còn lại
        if (user == null) {
            System.out.println("⚠️ First search failed, trying alternative method");
            if (username.contains("@")) {
                user = userRepository.findByUsername(username);
            } else {
                user = userRepository.findByEmail(username);
            }
        }

        if (user == null) {
            System.err.println("❌ User not found: " + username);
            throw new UsernameNotFoundException("Không tìm thấy người dùng với tên đăng nhập hoặc email: " + username);
        }

        System.out.println("✅ User found: " + user.getUsername() + " (ID: " + user.getId() + ")");

        // Kiểm tra trạng thái tài khoản
        if (!user.isActive()) {
            System.err.println("❌ Account disabled: " + username);
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + username);
        }

        if (user.getAccountStatus() == User.AccountStatus.BANNED) {
            System.err.println("❌ Account banned: " + username);
            throw new UsernameNotFoundException("Tài khoản đã bị cấm: " + username);
        }

        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            System.err.println("❌ Account suspended: " + username);
            throw new UsernameNotFoundException("Tài khoản đã bị tạm khóa: " + username);
        }

        System.out.println("✅ Creating UserPrincipal for: " + user.getUsername());
        return new CustomUserPrincipal(user);
    }
}