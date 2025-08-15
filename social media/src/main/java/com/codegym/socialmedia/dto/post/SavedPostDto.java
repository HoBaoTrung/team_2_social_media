package com.codegym.socialmedia.dto.post;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.SavedPost;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SavedPostDto {
    private Long id;
    private PostDisplayDto post;
    private String collectionName;
    private LocalDateTime savedAt;

    public SavedPostDto(SavedPost savedPost, User currentUser) {
        this.id = savedPost.getId();

        // Sử dụng constructor đúng: (Post, isLiked, canEdit, canDelete)
        // Saved posts thường không cho phép edit/delete và cần check like status
        boolean isLiked = checkIfUserLikedPost(savedPost.getPost(), currentUser);
        boolean canEdit = checkIfUserCanEditPost(savedPost.getPost(), currentUser);
        boolean canDelete = checkIfUserCanDeletePost(savedPost.getPost(), currentUser);

        this.post = new PostDisplayDto(savedPost.getPost(), isLiked, canEdit, canDelete);
        this.collectionName = savedPost.getCollectionName();
        this.savedAt = savedPost.getCreatedAt();
    }

    // Helper methods để check permissions
    private boolean checkIfUserLikedPost(com.codegym.socialmedia.model.social_action.Post post, User user) {
        if (user == null || post == null) return false;
        // Đây là placeholder - trong thực tế sẽ check trong database
        // Có thể inject PostLikeRepository hoặc PostService để check
        return false; // Default: chưa like
    }

    private boolean checkIfUserCanEditPost(com.codegym.socialmedia.model.social_action.Post post, User user) {
        if (user == null || post == null || post.getUser() == null) return false;
        return post.getUser().getId().equals(user.getId());
    }

    private boolean checkIfUserCanDeletePost(com.codegym.socialmedia.model.social_action.Post post, User user) {
        // Thường edit và delete có cùng quyền
        return checkIfUserCanEditPost(post, user);
    }
}