package com.codegym.socialmedia.model.account;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_privacy_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivacySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Quyền xem các thông tin cá nhân
    @Enumerated(EnumType.STRING)
    private PrivacyLevel showProfile = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showFriendList = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showFullName = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showAddress = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showPhone = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showEmail = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showAvatar = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showBio = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel showDob = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel allowSendMessage = PrivacyLevel.FRIENDS;

    // Tùy chọn tìm kiếm
    private boolean allowSearchByEmail = true;
    private boolean allowSearchByPhone = true;

    // Tùy chọn kết bạn
    private boolean allowFriendRequests = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum PrivacyLevel {
        PUBLIC, FRIENDS, PRIVATE
    }
}

