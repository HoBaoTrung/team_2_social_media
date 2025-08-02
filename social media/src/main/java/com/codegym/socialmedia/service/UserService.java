package com.codegym.socialmedia.service;

import com.codegym.socialmedia.dto.UserRegistrationDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.account.UserPrivacySettings;
import com.codegym.socialmedia.model.account.NotificationSettings;
import com.codegym.socialmedia.repository.UserRepository;
import com.codegym.socialmedia.repository.UserPrivacySettingsRepository;
import com.codegym.socialmedia.repository.NotificationSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPrivacySettingsRepository userPrivacySettingsRepository;

    @Autowired
    private NotificationSettingsRepository notificationSettingsRepository;

    @Autowired
    @Lazy // Thêm @Lazy để tránh circular dependency
    private PasswordEncoder passwordEncoder;

    public User save(UserRegistrationDto registrationDto) {
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhone(registrationDto.getPhone());
        user.setDateOfBirth(registrationDto.getDateOfBirth());
        user.setLoginMethod(User.LoginMethod.EMAIL);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setActive(true);
        user.setVerified(false);
        user.setCanBeFound(true);
        user.setShowFriendList(true);
        user.setPrivacyProfile(User.PrivacyProfile.PUBLIC);

        User savedUser = userRepository.save(user);

        // Tạo privacy settings mặc định
        createDefaultPrivacySettings(savedUser);

        // Tạo notification settings mặc định
        createDefaultNotificationSettings(savedUser);

        return savedUser;
    }

    private void createDefaultPrivacySettings(User user) {
        UserPrivacySettings privacySettings = new UserPrivacySettings();
        privacySettings.setUser(user);
        privacySettings.setAllowFriendRequests(true);
        privacySettings.setShowProfileToStrangers(true);
        privacySettings.setShowFriendListToPublic(true);
        privacySettings.setShowFriendListToFriends(true);
        privacySettings.setAllowSearchByEmail(true);
        privacySettings.setAllowSearchByPhone(true);
        privacySettings.setWallPostPrivacy(UserPrivacySettings.WallPostPrivacy.PUBLIC);

        userPrivacySettingsRepository.save(privacySettings);
    }

    private void createDefaultNotificationSettings(User user) {
        NotificationSettings notificationSettings = new NotificationSettings();
        notificationSettings.setUser(user);
        notificationSettings.setFriendRequests(true);
        notificationSettings.setMessages(true);
        notificationSettings.setStatusLikes(true);
        notificationSettings.setStatusComments(true);
        notificationSettings.setStatusShares(true);
        notificationSettings.setFriendStatusUpdates(true);
        notificationSettings.setSystemNotifications(false);
        notificationSettings.setEmailNotifications(false);
        notificationSettings.setPushNotifications(false);

        notificationSettingsRepository.save(notificationSettings);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public User createOrUpdateOAuth2User(String email, String name, String provider) {
        User user = findByEmail(email);

        if (user == null) {
            // Tạo user mới cho OAuth2
            user = new User();
            user.setEmail(email);

            // Tạo username unique từ email
            String baseUsername = email.split("@")[0];
            String username = generateUniqueUsername(baseUsername);
            user.setUsername(username);

            user.setPasswordHash(""); // OAuth2 không cần password

            // Tách firstName và lastName từ name
            if (name != null && !name.isEmpty()) {
                String[] nameParts = name.split(" ", 2);
                user.setFirstName(nameParts[0]);
                if (nameParts.length > 1) {
                    user.setLastName(nameParts[1]);
                }
            }

            // Set login method dựa trên provider
            if ("google".equalsIgnoreCase(provider)) {
                user.setLoginMethod(User.LoginMethod.GOOGLE);
            } else if ("facebook".equalsIgnoreCase(provider)) {
                user.setLoginMethod(User.LoginMethod.FACEBOOK);
            } else {
                user.setLoginMethod(User.LoginMethod.EMAIL);
            }

            user.setAccountStatus(User.AccountStatus.ACTIVE);
            user.setActive(true);
            user.setVerified(true); // OAuth2 user đã được verify
            user.setCanBeFound(true);
            user.setShowFriendList(true);
            user.setPrivacyProfile(User.PrivacyProfile.PUBLIC);

            User savedUser = userRepository.save(user);

            // Tạo settings mặc định cho OAuth2 user
            createDefaultPrivacySettings(savedUser);
            createDefaultNotificationSettings(savedUser);

            return savedUser;
        } else {
            // Cập nhật thông tin user hiện có
            if (name != null && !name.isEmpty()) {
                String[] nameParts = name.split(" ", 2);
                if (user.getFirstName() == null || user.getFirstName().isEmpty()) {
                    user.setFirstName(nameParts[0]);
                }
                if (nameParts.length > 1 && (user.getLastName() == null || user.getLastName().isEmpty())) {
                    user.setLastName(nameParts[1]);
                }
            }

            // Cập nhật login method nếu chưa có
            if (user.getLoginMethod() == User.LoginMethod.EMAIL) {
                if ("google".equalsIgnoreCase(provider)) {
                    user.setLoginMethod(User.LoginMethod.GOOGLE);
                } else if ("facebook".equalsIgnoreCase(provider)) {
                    user.setLoginMethod(User.LoginMethod.FACEBOOK);
                }
            }

            return userRepository.save(user);
        }
    }

    private String generateUniqueUsername(String baseUsername) {
        String username = baseUsername;
        int counter = 1;

        // Loại bỏ ký tự đặc biệt
        username = username.replaceAll("[^a-zA-Z0-9_]", "");

        // Đảm bảo username không trống
        if (username.isEmpty()) {
            username = "user";
        }

        // Kiểm tra và tạo username unique
        while (existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    // Thêm các method debug
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long countUsers() {
        return userRepository.count();
    }

    public void deleteAllUsers() {
        userRepository.deleteAll();
    }
}