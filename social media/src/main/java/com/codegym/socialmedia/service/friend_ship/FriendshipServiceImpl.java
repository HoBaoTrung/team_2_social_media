package com.codegym.socialmedia.service.friend_ship;

import com.codegym.socialmedia.dto.friend.FriendDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.account.UserPrivacySettings;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.FriendshipId;
import com.codegym.socialmedia.repository.FriendshipRepository;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.service.user.UserService;
import com.codegym.socialmedia.service.user.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendshipServiceImpl implements FriendshipService {
    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private UserServiceImpl userServiceImpl;

    @Override
    public boolean addFriendship(User user) {
        User currentUser = userService.getCurrentUser();
        FriendshipId friendshipId = new FriendshipId();
        friendshipId.setRequesterId(currentUser.getId());
        friendshipId.setAddresseeId(user.getId());

        Friendship newFriendship = new Friendship();
        newFriendship.setId(friendshipId);
        newFriendship.setAddressee(user);
        newFriendship.setRequester(currentUser);
        newFriendship.setStatus(Friendship.FriendshipStatus.PENDING);
        try {
            friendshipRepository.save(newFriendship);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean acceptFriendship(User user) {
        Friendship f = findByUsers(user.getId(), userService.getCurrentUser().getId());
        f.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        try {
            friendshipRepository.save(f);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }


    @Override
    public boolean deleteFriendship(User user) {
        Friendship f = findByUsers(user.getId(), userService.getCurrentUser().getId());
        try {
            friendshipRepository.delete(f);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int countMutualFriends(Long userAId, Long userBId){
       return friendshipRepository.countMutualFriends(userAId, userBId);
    }

    @Override
    public List<FriendDto> getVisibleFriendList(User targetUser) {
        UserPrivacySettings.PrivacyLevel level = targetUser.getPrivacySettings().getShowFriendList();
        User viewer = userService.getCurrentUser();
        boolean isOwner = viewer.getId().equals(targetUser.getId());

        boolean isFriend = getFriendshipStatus(targetUser, viewer) == Friendship.FriendshipStatus.ACCEPTED;
        if (level == UserPrivacySettings.PrivacyLevel.PRIVATE && isFriend) {
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

        List<FriendDto> result = new ArrayList<>();
        for (User user : allFriends) {
            int mutualCount = countMutualFriends(user.getId(), viewer.getId());
            result.add(new FriendDto(user, mutualCount));
        }
        return result;
    }

    @Override
    public Friendship findByUsers(Long userId1, Long userId2) {
        return friendshipRepository.findByRequesterIdAndAddresseeId(userId1, userId2)
                .or(() -> friendshipRepository.findByRequesterIdAndAddresseeId(userId2, userId1))
                .orElse(null);
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

    @Override
    public List<FriendDto> findMutualFriends(Long userAId, Long userBId) {
        List<User> list = friendshipRepository.findMutualFriends(userAId, userBId);
        List<FriendDto> result = new ArrayList<>();
        for (User user : list) {
            int mutualCount = countMutualFriends(userAId, userBId);
            result.add(new FriendDto(user, mutualCount));
        }
        return result;
    }


    @Override
    public int countFriends(Long userId) {
        return friendshipRepository.countFriendsByUserId(userId);
    }

//    public boolean areFriends(User user1, User user2) {
//        if (user1 == null || user2 == null) return false;
//        return friendshipRepository.findAcceptedFriendshipBetween(user1, user2).isPresent();
//    }

    public Friendship.FriendshipStatus getFriendshipStatus(User user1, User user2) {
        if (user1 == null || user2 == null || user1.equals(user2)) {
            return Friendship.FriendshipStatus.NONE; // Không so sánh với chính mình hoặc null
        }

        Optional<Friendship> optional = friendshipRepository.findFriendshipBetween(user1, user2);

        if (!optional.isPresent()) {
            return Friendship.FriendshipStatus.NONE;
        }

        Friendship friendship = optional.get();
        Friendship.FriendshipStatus status;

        switch (friendship.getStatus()) {
            case PENDING:
                if (friendship.getRequester().equals(user1)) {
                    // user1 đã gửi yêu cầu
                    status = Friendship.FriendshipStatus.PENDING;
                } else {
                    // user1 là người nhận, có thể chấp nhận
                    status = Friendship.FriendshipStatus.PENDING;
                }
                break;
            case ACCEPTED:
                status = Friendship.FriendshipStatus.ACCEPTED;
                break;
            default:
                status = Friendship.FriendshipStatus.NONE;
                break;
        }

        return status;
    }

}
