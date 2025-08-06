package com.codegym.socialmedia.dto.friend;

import com.codegym.socialmedia.model.account.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FriendDto {
    private String username;
    private String fullName;
    private String avatarUrl;
    private int mutualFriends;

    public FriendDto(User user, int mutualFriends) {
        this.username = user.getUsername();
        this.avatarUrl = user.getProfilePicture();
        this.fullName = user.getFirstName() + " " + user.getLastName();
        this.mutualFriends = mutualFriends;
    }
}
