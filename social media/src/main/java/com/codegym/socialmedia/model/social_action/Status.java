// Status.java - Updated Entity
package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "statuses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Status {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(columnDefinition = "JSON")
    private String imageUrls; // Lưu danh sách URL ảnh dạng JSON

    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    private StatusType statusType = StatusType.TEXT;

    @ManyToOne
    @JoinColumn(name = "shared_status_id")
    private Status sharedStatus;

    private int shareCount = 0;
    private int likeCount = 0;
    private int commentCount = 0;

    private boolean isDeleted = false;
    private boolean isPinned = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relations
    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StatusPrivacy> privacySettings;

    @OneToMany(mappedBy = "status", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LikeStatus> likedByUsers;

    // Enums
    public enum PrivacyLevel {
        PUBLIC("Công khai", "fas fa-globe-americas"),
        FRIENDS("Bạn bè", "fas fa-user-friends"),
        ONLY_ME("Chỉ mình tôi", "fas fa-lock");

        private final String displayName;
        private final String iconClass;

        PrivacyLevel(String displayName, String iconClass) {
            this.displayName = displayName;
            this.iconClass = iconClass;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getIconClass() {
            return iconClass;
        }
    }

    public enum StatusType {
        TEXT("Văn bản"),
        IMAGE("Hình ảnh"),
        VIDEO("Video"),
        SHARED("Chia sẻ");

        private final String displayName;

        StatusType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods
    public boolean isLikedByUser(User user) {
        if (likedByUsers == null || user == null) return false;
        return likedByUsers.stream()
                .anyMatch(like -> like.getUser().getId().equals(user.getId()));
    }

    public boolean canBeViewedBy(User viewer) {
        if (isDeleted) return false;

        // Chủ sở hữu luôn xem được
        if (viewer != null && viewer.getId().equals(this.user.getId())) {
            return true;
        }

        switch (privacyLevel) {
            case PUBLIC:
                return true;
            case FRIENDS:
                // Kiểm tra có phải bạn bè không (cần implement logic friendship)
                return viewer != null && areFriends(viewer, this.user);
            case ONLY_ME:
                return viewer != null && viewer.getId().equals(this.user.getId());
            default:
                return false;
        }
    }

    public boolean canBeEditedBy(User user) {
        return user != null && user.getId().equals(this.user.getId()) && !isDeleted;
    }

    // Placeholder cho logic kiểm tra bạn bè - cần implement sau
    private boolean areFriends(User user1, User user2) {
        // TODO: Implement friendship logic
        return true; // Tạm thời return true
    }
}