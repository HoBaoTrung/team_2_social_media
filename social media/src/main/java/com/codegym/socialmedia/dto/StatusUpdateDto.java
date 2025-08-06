// StatusUpdateDto.java
package com.codegym.socialmedia.dto;

import com.codegym.socialmedia.model.social_action.Status;
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
public class StatusUpdateDto {
    private Long id;

    @NotBlank(message = "Nội dung không được để trống")
    @Size(max = 5000, message = "Nội dung không được vượt quá 5000 ký tự")
    private String content;

    private Status.PrivacyLevel privacyLevel;

    private List<MultipartFile> newImages;

    // Danh sách URL ảnh hiện tại cần giữ lại
    private List<String> keepImageUrls;

    // Danh sách URL ảnh cần xóa
    private List<String> removeImageUrls;

    // Xóa video hiện tại
    private Boolean removeVideo = false;
}
