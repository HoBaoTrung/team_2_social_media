package com.codegym.socialmedia.service.friend_ship;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.account.UserPrivacySettings;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.repository.FriendshipRepository;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FriendshipServiceImpl implements FriendshipService {
    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private UserService userService;

    public List<User> getVisibleFriendList(User targetUser) {
        UserPrivacySettings.PrivacyLevel level = targetUser.getPrivacySettings().getShowFriendList();
        User viewer = userService.getCurrentUser();
        boolean isOwner = viewer.getId().equals(targetUser.getId());
        boolean isFriend = areFriends(targetUser, viewer);
        if (level == UserPrivacySettings.PrivacyLevel.PRIVATE && isFriend){
            return findMutualFriends(targetUser.getId(), viewer.getId());
        }
        // PRIVATE: chỉ chính chủ được xem
        if (level == UserPrivacySettings.PrivacyLevel.PRIVATE && !isOwner) {
            return Collections.emptyList();
        }

        // FRIENDS: chỉ bạn bè hoặc chính chủ được xem
        if (level == UserPrivacySettings.PrivacyLevel.FRIENDS && !(isFriend || isOwner)) {
            return Collections.emptyList();
        }




        // PUBLIC hoặc đủ điều kiện ở trên: lấy danh sách bạn bè (loại bỏ viewer khỏi danh sách)
        List<User> allFriends = friendshipRepository.findFriendsOfUserExcludingViewer(targetUser.getId(), viewer.getId());

        // Nếu là PUBLIC và viewer là bạn → đưa bạn chung lên đầu
        if ((level == UserPrivacySettings.PrivacyLevel.FRIENDS ||
                level == UserPrivacySettings.PrivacyLevel.PUBLIC) && isFriend) {
            Set<Long> viewerFriends = findFriendIdsOfUser(viewer.getId());
            allFriends.sort((u1, u2) -> {
                boolean u1IsMutual = viewerFriends.contains(u1.getId());
                boolean u2IsMutual = viewerFriends.contains(u2.getId());

                if (u1IsMutual && !u2IsMutual) return -1;
                if (!u1IsMutual && u2IsMutual) return 1;
                return 0;
            });
        }

        return allFriends;
    }

    public Set<Long> findFriendIdsOfUser(Long userId) {
        List<Friendship> friendships = friendshipRepository.findAllFriendshipsOfUser(userId);

        return friendships.stream()
                .map(f -> {
                    Long requesterId = f.getRequester().getId();
                    Long addresseeId = f.getAddressee().getId();
                    return requesterId.equals(userId) ? addresseeId : requesterId;
                })
                .collect(Collectors.toSet());
    }

    public List<User> findMutualFriends(Long userAId, Long userBId) {
        List<Friendship> friendshipsA = friendshipRepository.findAllFriendshipsOfUser(userAId);
        List<Friendship> friendshipsB = friendshipRepository.findAllFriendshipsOfUser(userBId);

        Set<Long> friendsOfA = friendshipsA.stream()
                .map(f -> f.getRequester().getId().equals(userAId) ? f.getAddressee().getId() : f.getRequester().getId())
                .collect(Collectors.toSet());

        Set<Long> friendsOfB = friendshipsB.stream()
                .map(f -> f.getRequester().getId().equals(userBId) ? f.getAddressee().getId() : f.getRequester().getId())
                .collect(Collectors.toSet());

        friendsOfA.retainAll(friendsOfB); // chỉ giữ bạn chung

        if (friendsOfA.isEmpty()) return List.of();

        return userRepository.findAllById(friendsOfA);
    }



    @Override
    public List<User> getFriends(Long userId) {
//        List<Friendship> friendships = friendshipRepository.findFriendsByUserId(userId);
        return friendshipRepository.findFriendsOfUserExcludingViewer(userId, userService.getCurrentUser().getId());
//        List<User> friends = new ArrayList<>();

//        for (Friendship f : friendships) {
//            if (f.getRequester().getId().equals(userId)) {
//                friends.add(f.getAddressee()); // Nếu là người gửi lời mời → bạn là người nhận
//            } else {
//                friends.add(f.getRequester()); // Ngược lại
//            }
//        }
//
//        return friends;
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
