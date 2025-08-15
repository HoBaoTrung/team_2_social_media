package com.codegym.socialmedia.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostReactionDto {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String userFullName;
    private String userAvatarUrl;
    private String reactionType; // LIKE, LOVE, HAHA, etc.
    private LocalDateTime createdAt;
}