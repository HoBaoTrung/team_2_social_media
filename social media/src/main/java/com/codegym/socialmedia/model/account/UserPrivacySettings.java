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

    private boolean allowFriendRequests = true ;
    private boolean showProfileToStrangers = true;
    private boolean showFriendListToPublic = true;
    private boolean showFriendListToFriends = true;
    private boolean allowSearchByEmail = true;
    private boolean allowSearchByPhone  = true;

    @Enumerated(EnumType.STRING)
    private WallPostPrivacy wallPostPrivacy;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum WallPostPrivacy {
        PUBLIC, FRIENDS, PRIVATE
    }
}
