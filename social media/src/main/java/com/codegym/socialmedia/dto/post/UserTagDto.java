package com.codegym.socialmedia.dto.post;

import com.codegym.socialmedia.model.account.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTagDto {
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;

    public UserTagDto(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFirstName() + " " + user.getLastName();
        this.avatarUrl = user.getProfilePicture();
    }
}