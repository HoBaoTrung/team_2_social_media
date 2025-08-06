// StatusSearchDto.java
package com.codegym.socialmedia.dto;

import com.codegym.socialmedia.model.social_action.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusSearchDto {
    private String query;
    private Long userId; // Tìm kiếm trong status của user cụ thể
    private Status.PrivacyLevel privacyLevel;
    private int page = 0;
    private int size = 10;
    private String sort = "newest"; // newest, oldest, relevant, popular
}