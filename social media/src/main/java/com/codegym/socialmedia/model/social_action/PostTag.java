package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_tags",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "tagged_user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne
    @JoinColumn(name = "tagged_user_id", nullable = false)
    private User taggedUser;

    @ManyToOne
    @JoinColumn(name = "tagger_user_id", nullable = false)
    private User taggerUser;

    @CreationTimestamp
    private LocalDateTime createdAt;
}