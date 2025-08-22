// PostCreateDto.java
package com.codegym.socialmedia.dto.post;

import com.codegym.socialmedia.model.PrivacyLevel;
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

    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    private PrivacyLevel privacyLevel = PrivacyLevel.PUBLIC;

    private List<MultipartFile> images;
}
