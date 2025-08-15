package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ReactionType reactionType = ReactionType.LIKE;

    public enum ReactionType {
        LIKE("Thích"),
        LOVE("Yêu thích"),
        HAHA("Haha"),
        WOW("Wow"),
        SAD("Buồn"),
        ANGRY("Tức giận");

        private final String displayName;

        ReactionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructor for backward compatibility
    public PostLike(Post post, User user) {
        this.post = post;
        this.user = user;
        this.reactionType = ReactionType.LIKE;
    }
}