package com.codegym.socialmedia.dto.post;

import com.codegym.socialmedia.model.social_action.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDisplayDto {

    private Long id;
    private String content;
    private List<String> imageUrls;
    private List<String> videoUrls;
    private Post.PrivacyLevel privacyLevel;
    private Post.PostType postType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User info
    private Long userId;
    private String username;
    private String userFullName;
    private String userAvatarUrl;

    // Metadata
    private String location;
    private String feeling;
    private String activity;

    // For shared posts
    private PostDisplayDto originalPost;
    private String shareText;
    private List<String> taggedUsernames;

    // Stats
    private int likesCount;
    private int commentsCount;
    private int sharesCount;
    private boolean isLikedByCurrentUser;
    private boolean canEdit;
    private boolean canDelete;

    public PostDisplayDto(Post post, boolean isLikedByCurrentUser, boolean canEdit, boolean canDelete) {
        this.id = post.getId();
        this.content = post.getContent();
        this.imageUrls = parseImageUrls(post.getImageUrls());
        this.videoUrls = parseImageUrls(post.getVideoUrls());
        this.privacyLevel = post.getPrivacyLevel();
        this.postType = post.getPostType();
        this.createdAt = post.getCreatedAt();
        this.updatedAt = post.getUpdatedAt();

        this.userId = post.getUser().getId();
        this.username = post.getUser().getUsername();
        this.userFullName = post.getUser().getFirstName() + " " + post.getUser().getLastName();
        this.userAvatarUrl = post.getUser().getProfilePicture();

        // Metadata
        this.location = post.getLocation();
        this.feeling = post.getFeeling();
        this.activity = post.getActivity();
        this.shareText = post.getShareText();
        this.taggedUsernames = parseImageUrls(post.getTaggedUsernames());

        // Handle original post for shares - sử dụng constructor đơn giản để tránh recursive
        if (post.getOriginalPost() != null) {
            this.originalPost = createSimplePostDto(post.getOriginalPost());
        }

        this.likesCount = post.getLikesCount();
        this.commentsCount = post.getCommentsCount();
        this.sharesCount = post.getSharesCount();
        this.isLikedByCurrentUser = isLikedByCurrentUser;
        this.canEdit = canEdit;
        this.canDelete = canDelete;
    }

    // Constructor đơn giản cho nested posts (originalPost)
    private PostDisplayDto createSimplePostDto(Post post) {
        PostDisplayDto dto = new PostDisplayDto();
        dto.id = post.getId();
        dto.content = post.getContent();
        dto.imageUrls = parseImageUrls(post.getImageUrls());
        dto.videoUrls = parseImageUrls(post.getVideoUrls());
        dto.privacyLevel = post.getPrivacyLevel();
        dto.postType = post.getPostType();
        dto.createdAt = post.getCreatedAt();
        dto.updatedAt = post.getUpdatedAt();

        dto.userId = post.getUser().getId();
        dto.username = post.getUser().getUsername();
        dto.userFullName = post.getUser().getFirstName() + " " + post.getUser().getLastName();
        dto.userAvatarUrl = post.getUser().getProfilePicture();

        dto.location = post.getLocation();
        dto.feeling = post.getFeeling();
        dto.activity = post.getActivity();
        dto.taggedUsernames = parseImageUrls(post.getTaggedUsernames());

        dto.likesCount = post.getLikesCount();
        dto.commentsCount = post.getCommentsCount();
        dto.sharesCount = post.getSharesCount();

        // Nested post không có quyền edit/delete và không check like
        dto.isLikedByCurrentUser = false;
        dto.canEdit = false;
        dto.canDelete = false;

        return dto;
    }

    private List<String> parseImageUrls(String imageUrlsJson) {
        if (imageUrlsJson == null || imageUrlsJson.trim().isEmpty()) {
            return List.of();
        }
        // Parse JSON array string to List<String>
        try {
            // Simple JSON parsing - in production, use Jackson
            String cleaned = imageUrlsJson.replaceAll("[\\[\\]\"]", "");
            if (cleaned.trim().isEmpty()) {
                return List.of();
            }
            return List.of(cleaned.split(","));
        } catch (Exception e) {
            return List.of();
        }
    }

    // Helper methods
    public boolean hasImages() {
        return imageUrls != null && !imageUrls.isEmpty();
    }

    public boolean hasVideos() {
        return videoUrls != null && !videoUrls.isEmpty();
    }

    public boolean hasMedia() {
        return hasImages() || hasVideos();
    }

    public boolean isSharedPost() {
        return originalPost != null;
    }

    public boolean hasLocation() {
        return location != null && !location.trim().isEmpty();
    }

    public boolean hasFeeling() {
        return feeling != null && !feeling.trim().isEmpty();
    }

    public boolean hasActivity() {
        return activity != null && !activity.trim().isEmpty();
    }

    public boolean hasTaggedUsers() {
        return taggedUsernames != null && !taggedUsernames.isEmpty();
    }

    public String getPostTypeDisplayName() {
        return postType != null ? postType.getDisplayName() : "Văn bản";
    }

    public String getPrivacyDisplayName() {
        return privacyLevel != null ? privacyLevel.getDisplayName() : "Công khai";
    }
}