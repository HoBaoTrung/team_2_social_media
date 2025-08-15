package com.codegym.socialmedia.model.account;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

// Thêm vào UserPrivacySettings.java
@Entity
@Table(name = "user_privacy_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivacySettings {

    @Id
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Các trường privacy levels
    @Enumerated(EnumType.STRING)
    @Column(name = "show_profile", nullable = false)
    private PrivacyLevel showProfile = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_friend_list", nullable = false)
    private PrivacyLevel showFriendList = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_full_name", nullable = false)
    private PrivacyLevel showFullName = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_address", nullable = false)
    private PrivacyLevel showAddress = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_phone", nullable = false)
    private PrivacyLevel showPhone = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_email", nullable = false)
    private PrivacyLevel showEmail = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_avatar", nullable = false)
    private PrivacyLevel showAvatar = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_bio", nullable = false)
    private PrivacyLevel showBio = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "show_dob", nullable = false)
    private PrivacyLevel showDob = PrivacyLevel.PRIVATE;

    @Enumerated(EnumType.STRING)
    @Column(name = "allow_send_message", nullable = false)
    private PrivacyLevel allowSendMessage = PrivacyLevel.FRIENDS;

    // Các boolean fields với default values
    @Column(name = "allow_search_by_email", nullable = false)
    private boolean allowSearchByEmail = true;

    @Column(name = "allow_search_by_phone", nullable = false)
    private boolean allowSearchByPhone = true;

    @Column(name = "can_be_found", nullable = false)
    private boolean canBeFound = true; // ✅ QUAN TRỌNG: Thêm default value

    @Column(name = "allow_friend_requests", nullable = false)
    private boolean allowFriendRequests = true;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PrivacyLevel {
        PUBLIC, FRIENDS, PRIVATE
    }
}