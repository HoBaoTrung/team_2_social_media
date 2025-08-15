package com.codegym.socialmedia.dto.post;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDto {

    private Long id;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User info
    private Long userId;
    private String username;
    private String userFullName;
    private String userAvatarUrl;

    // Permissions
    private boolean canEdit;
    private boolean canDelete;
}