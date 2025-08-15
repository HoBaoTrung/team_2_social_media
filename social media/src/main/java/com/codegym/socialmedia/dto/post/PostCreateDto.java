package com.codegym.socialmedia.dto.post;

import com.codegym.socialmedia.model.social_action.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDto {

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    private Post.PrivacyLevel privacyLevel = Post.PrivacyLevel.PUBLIC;

    private List<MultipartFile> images;
    private List<MultipartFile> videos;

    // Metadata
    private String location;
    private String feeling;
    private String activity;

    // For sharing
    private Long originalPostId; // If sharing another post
    private String shareText;

    // Tagged friends
    private List<String> taggedUsernames;

    // Post type will be determined automatically based on content
    public Post.PostType determinePostType() {
        boolean hasImages = images != null && !images.isEmpty();
        boolean hasVideos = videos != null && !videos.isEmpty();
        boolean isSharing = originalPostId != null;

        if (isSharing) {
            return Post.PostType.SHARED;
        } else if (hasVideos) {
            // Ưu tiên video nếu có cả video và ảnh
            return Post.PostType.VIDEO;
        } else if (hasImages) {
            return Post.PostType.IMAGE;
        } else {
            return Post.PostType.TEXT;
        }
    }

    // Helper methods
    public boolean hasMedia() {
        return (images != null && !images.isEmpty()) ||
                (videos != null && !videos.isEmpty());
    }

    public boolean isSharePost() {
        return originalPostId != null;
    }

    public int getTotalMediaCount() {
        int count = 0;
        if (images != null) count += images.size();
        if (videos != null) count += videos.size();
        return count;
    }

    // Validation methods
    public boolean isValid() {
        // Post phải có nội dung hoặc media hoặc là share post
        return (content != null && !content.trim().isEmpty()) ||
                hasMedia() ||
                isSharePost();
    }

    public String getValidationMessage() {
        if (!isValid()) {
            return "Bài viết phải có nội dung, hình ảnh/video hoặc chia sẻ từ bài viết khác";
        }
        return null;
    }

    // Method to create PostUpdateDto from this DTO
    public PostUpdateDto toUpdateDto() {
        PostUpdateDto updateDto = new PostUpdateDto();
        updateDto.setContent(this.content);
        updateDto.setPrivacyLevel(this.privacyLevel);
        updateDto.setNewImages(this.images);
        // Note: videos will be handled separately if needed
        return updateDto;
    }
}