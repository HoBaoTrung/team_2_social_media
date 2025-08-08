// LikeStatusId.java - Fixed version  
package com.codegym.socialmedia.model.social_action;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeStatusId implements Serializable {
    // FIX: Thay đổi từ Integer thành Long
    private Long userId;
    private Long statusId;
}