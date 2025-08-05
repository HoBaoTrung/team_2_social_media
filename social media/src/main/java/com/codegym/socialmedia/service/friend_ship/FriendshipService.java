package com.codegym.socialmedia.service.friend_ship;

import com.codegym.socialmedia.model.account.User;

import java.util.List;

public interface FriendshipService {
    List<User> getFriends(Long userId);
    long countFriends(Long userId);
    boolean areFriends(User user1, User user2);
}
