package com.codegym.socialmedia.dto.post;

import com.codegym.socialmedia.model.social_action.Post;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostShareDto {
    private Long originalPostId;

    @Size(max = 1000, message = "Nội dung chia sẻ không được vượt quá 1000 ký tự")
    private String shareText;

    private Post.PrivacyLevel privacyLevel = Post.PrivacyLevel.PUBLIC;
}