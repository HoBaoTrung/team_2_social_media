// StatusResponseDto.java
package com.codegym.socialmedia.dto;

import com.codegym.socialmedia.model.social_action.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusResponseDto {
    private Long id;
    private String content;
    private List<String> imageUrls;
    private String videoUrl;
    private Status.PrivacyLevel privacyLevel;
    private Status.StatusType statusType;
    private int likeCount;
    private int commentCount;
    private int shareCount;
    private boolean isLiked;
    private boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // User info
    private Long userId;
    private String userFullName;
    private String userAvatar;
    private String username;

    // Shared status info (nếu có)
    private StatusResponseDto sharedStatus;

    // Để hiển thị thời gian theo kiểu Facebook
    public String getTimeAgo() {
        if (createdAt == null) return "vừa xong";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(createdAt, now);

        if (minutes < 1) {
            return "vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút trước";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + " giờ trước";
        } else if (minutes < 43200) { // 30 days
            long days = minutes / 1440;
            return days + " ngày trước";
        } else {
            // Format as date for older posts
            return createdAt.toLocalDate().toString();
        }
    }
}