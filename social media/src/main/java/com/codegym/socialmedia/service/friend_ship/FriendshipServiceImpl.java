package com.codegym.socialmedia.service.friend_ship;

import com.codegym.socialmedia.dto.friend.FriendDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.account.UserPrivacySettings;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.FriendshipId;
import com.codegym.socialmedia.repository.FriendshipRepository;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FriendshipServiceImpl implements FriendshipService {

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public boolean addFriendship(User user) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null || user == null || currentUser.getId().equals(user.getId())) {
            return false;
        }

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
        User currentUser = userService.getCurrentUser();
        if (currentUser == null || user == null) {
            return false;
        }

        Friendship friendship = findByUsers(user.getId(), currentUser.getId());
        if (friendship == null || friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            return false;
        }

        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        try {
            friendshipRepository.save(friendship);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteFriendship(User user) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null || user == null) {
            return false;
        }

        Friendship friendship = findByUsers(user.getId(), currentUser.getId());
        if (friendship == null) {
            return false;
        }

        try {
            friendshipRepository.delete(friendship);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int countMutualFriends(Long userAId, Long userBId) {
        return friendshipRepository.countMutualFriends(userAId, userBId);
    }

    @Override
    public Page<FriendDto> getVisibleFriendList(User targetUser, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        User viewer = userService.getCurrentUser();
        if (viewer == null || targetUser == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        boolean isOwner = viewer.getId().equals(targetUser.getId());
        UserPrivacySettings.PrivacyLevel level = targetUser.getPrivacySettings().getShowFriendList();
        boolean isFriend = getFriendshipStatus(targetUser, viewer) == Friendship.FriendshipStatus.ACCEPTED;

        // PRIVATE: chỉ chính chủ được xem hoặc bạn bè xem bạn chung
        if (level == UserPrivacySettings.PrivacyLevel.PRIVATE) {
            if (isOwner) {
                return getFriendsPage(targetUser.getId(), viewer.getId(), pageable);
            } else if (isFriend) {
                return findMutualFriends(targetUser.getId(), viewer.getId(), page, size);
            }
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // FRIENDS: chỉ bạn bè hoặc chính chủ được xem
        if (level == UserPrivacySettings.PrivacyLevel.FRIENDS && !(isFriend || isOwner)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // PUBLIC hoặc đủ điều kiện: lấy danh sách bạn bè
        return getFriendsPage(targetUser.getId(), viewer.getId(), pageable);
    }
    private Page<FriendDto> getFriendsPage(Long targetUserId, Long viewerId, Pageable pageable) {
        Page<User> friendsPage = friendshipRepository.findFriendsOfUserExcludingViewer(targetUserId, viewerId, pageable);

        List<FriendDto> friendDtos = friendsPage.getContent().stream()
                .filter(user -> !user.getId().equals(viewerId)) // Loại bỏ viewer khỏi danh sách
                .map(user -> new FriendDto(user, countMutualFriends(user.getId(), viewerId)))
                .collect(Collectors.toList());

        return new PageImpl<>(friendDtos, pageable, friendsPage.getTotalElements());
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
                .map(f -> f.getRequester().getId().equals(userId) ? f.getAddressee().getId() : f.getRequester().getId())
                .collect(Collectors.toSet());
    }

    @Override
    public Page<FriendDto> findMutualFriends(Long userAId, Long userBId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> mutualFriendsPage = friendshipRepository.findMutualFriends(userAId, userBId, pageable);
        long currentUserID = userService.getCurrentUser().getId();
        List<FriendDto> friendDtos = mutualFriendsPage.getContent().stream()
                .map(user -> new FriendDto(user, countMutualFriends(currentUserID, user.getId())))
                .collect(Collectors.toList());

        return new PageImpl<>(friendDtos, pageable, mutualFriendsPage.getTotalElements());
    }

    @Override
    public Page<User> findFriendsWithAllowSendMessage(User u, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        long currentUserID = userService.getCurrentUser().getId();
        return friendshipRepository.findFriendsWithAllowSendMessage(u.getId(),pageable );
    }

    @Override
    public int countFriends(Long userId) {
        return friendshipRepository.countFriendsByUserId(userId);
    }

    @Override
    public Friendship.FriendshipStatus getFriendshipStatus(User user1, User user2) {
        if (user1 == null || user2 == null || user1.equals(user2)) {
            return Friendship.FriendshipStatus.NONE;
        }

        Optional<Friendship> optional = friendshipRepository.findFriendshipBetween(user1, user2);
        if (!optional.isPresent()) {
            return Friendship.FriendshipStatus.NONE;
        }

        return optional.get().getStatus();
    }


    @Override
    public Page<FriendDto> findNonFriends(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> nonFriendsPage = friendshipRepository.findNonFriends(currentUserId, pageable);

        List<FriendDto> friendDtos = nonFriendsPage.getContent().stream()
                .map(user -> new FriendDto(user, countMutualFriends(currentUserId, user.getId())))
                .collect(Collectors.toList());

        return new PageImpl<>(friendDtos, pageable, nonFriendsPage.getTotalElements());
    }

    @Override
    public Page<FriendDto> findSentFriendRequests(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> sentRequestsPage = friendshipRepository.findSentFriendRequests(currentUserId, pageable);

        List<FriendDto> friendDtos = sentRequestsPage.getContent().stream()
                .map(user -> new FriendDto(user, countMutualFriends(currentUserId, user.getId())))
                .collect(Collectors.toList());

        return new PageImpl<>(friendDtos, pageable, sentRequestsPage.getTotalElements());
    }

    @Override
    public Page<FriendDto> findReceivedFriendRequests(Long currentUserId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> receivedRequestsPage = friendshipRepository.findReceivedFriendRequests(currentUserId, pageable);

        List<FriendDto> friendDtos = receivedRequestsPage.getContent().stream()
                .map(user -> new FriendDto(user, countMutualFriends(currentUserId, user.getId())))
                .collect(Collectors.toList());

        return new PageImpl<>(friendDtos, pageable, receivedRequestsPage.getTotalElements());
    }


}