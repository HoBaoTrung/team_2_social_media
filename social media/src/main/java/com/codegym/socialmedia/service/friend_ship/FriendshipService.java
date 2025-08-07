package com.codegym.socialmedia.service.friend_ship;

import com.codegym.socialmedia.dto.friend.FriendDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.FriendshipId;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FriendshipService {
    boolean addFriendship(User user);
    boolean acceptFriendship(User user);
    boolean deleteFriendship(User user);
    Page<FriendDto> getVisibleFriendList(User u, int page, int size);
    Friendship findByUsers(Long userId1, Long userId2);
    Page<FriendDto> findMutualFriends(Long userAId, Long userBId, int page, int size);

    int countFriends(Long userId);
    int countMutualFriends(Long userAId, Long userBId);

    Friendship.FriendshipStatus getFriendshipStatus(User user1, User user2);

    Page<FriendDto> findNonFriends(Long currentUserId, int page, int size);
    Page<FriendDto> findSentFriendRequests(Long currentUserId, int page, int size);
    Page<FriendDto> findReceivedFriendRequests(Long currentUserId, int page, int size);
}
