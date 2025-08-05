package com.codegym.socialmedia.service.friend_ship;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.repository.FriendshipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class FriendshipServiceImpl implements FriendshipService {
    @Autowired
    private FriendshipRepository friendshipRepository;
    @Override
    public List<User> getFriends(Long userId) {
        List<Friendship> friendships = friendshipRepository.findFriendsByUserId(userId);
        List<User> friends = new ArrayList<>();

        for (Friendship f : friendships) {
            if (f.getRequester().getId().equals(userId)) {
                friends.add(f.getAddressee()); // Nếu là người gửi lời mời → bạn là người nhận
            } else {
                friends.add(f.getRequester()); // Ngược lại
            }
        }

        return friends;
    }


    @Override
    public long countFriends(Long userId) {
        return friendshipRepository.countFriendsByUserId(userId);
    }

    public boolean areFriends(User user1, User user2) {
        if (user1 == null || user2 == null) return false;
        return friendshipRepository.findAcceptedFriendshipBetween(user1, user2).isPresent();
    }
}
