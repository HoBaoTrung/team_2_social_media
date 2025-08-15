package com.codegym.socialmedia.dto.status;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusDTO {
    private String content;
    private int likeCount;
    private int id;
    private boolean currentUserIsLiked;
}
