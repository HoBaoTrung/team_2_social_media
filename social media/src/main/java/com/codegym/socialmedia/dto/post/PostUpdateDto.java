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
public class PostUpdateDto {

    private Long id;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    private Post.PrivacyLevel privacyLevel;

    // New media files to upload
    private List<MultipartFile> newImages;
    private List<MultipartFile> newVideos;

    // Existing media URLs (to keep)
    private List<String> existingImages;
    private List<String> existingVideos;

    // Media URLs to delete
    private List<String> imagesToDelete;
    private List<String> videosToDelete;

    // Metadata updates
    private String location;
    private String feeling;
    private String activity;

    // Tagged users updates
    private List<String> taggedUsernames;

    // Helper methods
    public boolean hasNewMedia() {
        return (newImages != null && !newImages.isEmpty()) ||
                (newVideos != null && !newVideos.isEmpty());
    }

    public boolean hasExistingMedia() {
        return (existingImages != null && !existingImages.isEmpty()) ||
                (existingVideos != null && !existingVideos.isEmpty());
    }

    public boolean hasMediaToDelete() {
        return (imagesToDelete != null && !imagesToDelete.isEmpty()) ||
                (videosToDelete != null && !videosToDelete.isEmpty());
    }

    public int getTotalExistingMediaCount() {
        int count = 0;
        if (existingImages != null) count += existingImages.size();
        if (existingVideos != null) count += existingVideos.size();
        return count;
    }

    public int getTotalNewMediaCount() {
        int count = 0;
        if (newImages != null) count += newImages.size();
        if (newVideos != null) count += newVideos.size();
        return count;
    }

    // Validation
    public boolean isValid() {
        return (content != null && !content.trim().isEmpty()) ||
                hasNewMedia() ||
                hasExistingMedia();
    }

    public String getValidationMessage() {
        if (!isValid()) {
            return "Bài viết phải có nội dung hoặc hình ảnh/video";
        }
        return null;
    }
}