package com.codegym.socialmedia.dto.comment;

import com.codegym.socialmedia.dto.UserDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisplayCommentDTO {
    // User info
    private Long userId;
    private String username;
    private String userFullName;
    private String userAvatarUrl;

    // Comment info
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm")
    private LocalDateTime updatedAt;
    private String comment;
    private Long commentId;
    private int likeCount;
    private boolean isLikedByCurrentUser;
    private boolean canDeleted;
    private boolean canEdit;
    public DisplayCommentDTO(PostComment comment, User currentUser, boolean isLikedByCurrentUser) {
        this.userId = comment.getUser().getId();
        this.username = comment.getUser().getUsername();
        this.userAvatarUrl = comment.getUser().getProfilePicture();
        this.userFullName = comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.comment = comment.getContent();
        this.commentId = comment.getId();
        this.isLikedByCurrentUser = isLikedByCurrentUser;

        // Check quyền edit/delete
        if (currentUser != null) {
            this.canEdit = currentUser.getId().equals(comment.getUser().getId());
            this.canDeleted = currentUser.getId().equals(comment.getUser().getId());
        } else {
            this.canEdit = false;
            this.canDeleted = false;
        }
    }

}
