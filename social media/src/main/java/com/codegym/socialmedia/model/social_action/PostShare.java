package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_shares",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "original_post_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "original_post_id", nullable = false)
    private Post originalPost;

    @ManyToOne
    @JoinColumn(name = "shared_post_id", nullable = false)
    private Post sharedPost;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String shareText;

    @CreationTimestamp
    private LocalDateTime createdAt;
}