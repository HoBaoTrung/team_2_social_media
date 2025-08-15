package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_reactions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostReaction {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "post_id", nullable = false)
        private Post post;

        @ManyToOne
        @JoinColumn(name = "user_id", nullable = false)
        private User user;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        private ReactionType reactionType = ReactionType.LIKE;

        @CreationTimestamp
        private LocalDateTime createdAt;

        @UpdateTimestamp
        private LocalDateTime updatedAt;

        public enum ReactionType {
            LIKE("👍", "Thích"),
            LOVE("❤️", "Yêu thích"),
            HAHA("😂", "Haha"),
            WOW("😮", "Wow"),
            SAD("😢", "Buồn"),
            ANGRY("😡", "Tức giận");

            private final String emoji;
            private final String displayName;

            ReactionType(String emoji, String displayName) {
                this.emoji = emoji;
                this.displayName = displayName;
            }

            public String getEmoji() { return emoji; }
            public String getDisplayName() { return displayName; }
        }
    }

