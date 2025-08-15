package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 5000)
    private String content;

    @Column(columnDefinition = "JSON")
    private String imageUrls; // Lưu mảng URL ảnh dưới dạng JSON

    @Column(columnDefinition = "JSON")
    private String videoUrls; // Thêm field cho video URLs

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType = PostType.TEXT; // Thêm field mới

    @Column(nullable = false)
    private boolean isDeleted = false;

    // Cached count fields for performance
    @Column(name = "likes_count", columnDefinition = "INT DEFAULT 0")
    private int likesCount = 0;

    @Column(name = "comments_count", columnDefinition = "INT DEFAULT 0")
    private int commentsCount = 0;

    @Column(name = "shares_count", columnDefinition = "INT DEFAULT 0")
    private int sharesCount = 0;

    // Metadata fields
    private String location;
    private String feeling;
    private String activity;

    // For sharing posts
    @ManyToOne
    @JoinColumn(name = "original_post_id")
    private Post originalPost;

    @Column(length = 1000)
    private String shareText;

    @Column(columnDefinition = "JSON")
    private String taggedUsernames; // JSON array of tagged usernames

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostComment> comments;

    @OneToMany(mappedBy = "originalPost", cascade = CascadeType.ALL)
    private List<Post> shares; // Posts that share this post

    // Enums
    public enum PrivacyLevel {
        PUBLIC("Công khai"),
        FRIENDS("Bạn bè"),
        PRIVATE("Chỉ mình tôi");

        private final String displayName;

        PrivacyLevel(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PostType {
        TEXT("Văn bản"),
        IMAGE("Hình ảnh"),
        VIDEO("Video"),
        SHARED("Chia sẻ"),
        EVENT("Sự kiện"),
        POLL("Bình chọn");

        private final String displayName;

        PostType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Helper methods - sử dụng cached values từ database (ưu tiên)
    public int getLikesCount() {
        return likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public int getSharesCount() {
        return sharesCount; // Chỉ sử dụng cached value
    }

    // Methods to update cached counts
    public void setLikesCount(int likesCount) {
        this.likesCount = Math.max(0, likesCount);
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = Math.max(0, commentsCount);
    }

    public void setSharesCount(int sharesCount) {
        this.sharesCount = Math.max(0, sharesCount);
    }

    // Computed values từ relationships (fallback methods với tên khác)
    public int getComputedLikesCount() {
        return likes != null ? likes.size() : 0;
    }

    public int getComputedCommentsCount() {
        return comments != null ? comments.size() : 0;
    }

    public int getComputedSharesCount() {
        return shares != null ? shares.size() : 0;
    }

    // Utility methods
    public boolean isSharedPost() {
        return originalPost != null;
    }

    public boolean hasMedia() {
        return (imageUrls != null && !imageUrls.trim().isEmpty() && !imageUrls.equals("[]")) ||
                (videoUrls != null && !videoUrls.trim().isEmpty() && !videoUrls.equals("[]"));
    }

    // Determine post type based on content
    public void determineAndSetPostType() {
        if (originalPost != null) {
            this.postType = PostType.SHARED;
        } else if (videoUrls != null && !videoUrls.trim().isEmpty() && !videoUrls.equals("[]")) {
            this.postType = PostType.VIDEO;
        } else if (imageUrls != null && !imageUrls.trim().isEmpty() && !imageUrls.equals("[]")) {
            this.postType = PostType.IMAGE;
        } else {
            this.postType = PostType.TEXT;
        }
    }

    // Override toString để tránh lazy loading issues
    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", postType=" + postType +
                ", privacyLevel=" + privacyLevel +
                ", likesCount=" + likesCount +
                ", commentsCount=" + commentsCount +
                ", sharesCount=" + sharesCount +
                ", createdAt=" + createdAt +
                '}';
    }
}