package com.codegym.socialmedia.service.friend_ship;

import com.codegym.socialmedia.dto.friend.FriendDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.FriendshipId;

import java.util.List;

public interface FriendshipService {
    boolean addFriendship(User user);
    boolean acceptFriendship(User user);
    boolean deleteFriendship(User user);
    List<FriendDto> getVisibleFriendList(User u);
    Friendship findByUsers(Long userId1, Long userId2);
    List<FriendDto> findMutualFriends(Long userAId, Long userBId);

    int countFriends(Long userId);
    int countMutualFriends(Long userAId, Long userBId);
    //    boolean areFriends(User user1, User user2);
    Friendship.FriendshipStatus getFriendshipStatus(User user1, User user2);
}
