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
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Lob
    private String content;

    @Column(columnDefinition = "JSON")
    private String imageUrls;

    private String videoUrl;

    @Enumerated(EnumType.STRING)
    private PrivacyLevel privacyLevel;

    @Enumerated(EnumType.STRING)
    private StatusType statusType;

    @ManyToOne
    @JoinColumn(name = "shared_status_id")
    private Status sharedStatus;

    private int shareCount;

    private boolean isDeleted;
    private boolean isPinned;
    private boolean wallVisibility;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Mối quan hệ ngược với Comment
    @OneToMany(mappedBy = "status")
    private List<Comment> comments;

    @OneToMany(mappedBy = "status")
    private List<StatusPrivacy> privacySettings;

    @OneToMany(mappedBy = "status")
    private List<LikeStatus> likedByUsers;

    public enum PrivacyLevel {
        PUBLIC, FRIENDS, PRIVATE
    }

    public enum StatusType {
        TEXT, IMAGE, VIDEO, SHARE
    }
}
