package com.codegym.socialmedia.dto.comment;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisplayCommentDTO {
    private String id;

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
    private List<DisplayCommentDTO> replies;
    private Long parentCommentId;

    public DisplayCommentDTO(PostComment comment, boolean isLikedByCurrentUser) {
        this.userId = comment.getUser().getId();
        this.username = comment.getUser().getUsername();
        this.userAvatarUrl = comment.getUser().getProfilePicture();
        this.userFullName = comment.getUser().getFirstName() + " " + comment.getUser().getLastName();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.comment = comment.getContent();
        this.commentId = comment.getId();
        this.id=comment.getId().toString();
        this.isLikedByCurrentUser = isLikedByCurrentUser;
    }

    public static DisplayCommentDTO mapToDTO(PostComment comment, User currentUser) {
        DisplayCommentDTO dto = new DisplayCommentDTO();
        dto.setCommentId(comment.getId());
        dto.setComment(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setUserFullName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        dto.setUsername(comment.getUser().getUsername());
        dto.setUserAvatarUrl(comment.getUser().getProfilePicture());

        dto.setCanEdit(currentUser != null && comment.getUser().getId().equals(currentUser.getId()));
        dto.setCanDeleted(currentUser != null && comment.getUser().getId().equals(currentUser.getId()));
        dto.setParentCommentId(comment.getParent() != null ? comment.getParent().getId() : null);

        // Like info
        int likeCount = comment.getLikedByUsers() != null ? comment.getLikedByUsers().size() : 0;
        dto.setLikeCount(likeCount);

        boolean likedByCurrentUser = currentUser != null && comment.getLikedByUsers() != null &&
                comment.getLikedByUsers().stream().anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
        dto.setLikedByCurrentUser(likedByCurrentUser);
        // Replies (nếu có)
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(
                    comment.getReplies().stream()
                            .map(reply -> mapToDTO(reply, currentUser)) // đệ quy map reply
                            .collect(Collectors.toList())
            );
        }
        return dto;
    }
}
