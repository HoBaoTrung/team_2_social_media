package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.friend.FriendDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FriendController {
    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserService userService;

    @GetMapping("/friends")
    public String friends(Model model, @RequestParam(value = "filter", defaultValue = "all") String filter,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Page<FriendDto> friends;
        String listTitle;
        Long targetUserId = currentUser.getId(); // Mặc định hiển thị danh sách bạn bè của chính người dùng
        boolean isSender = false, isReceiver = false;
        switch (filter) {
            case "mutual":
                model.addAttribute("friendshipStatus", Friendship.FriendshipStatus.ACCEPTED.name());
                friends = friendshipService.findMutualFriends(currentUser.getId(), targetUserId, page, size);
                listTitle = "Bạn chung";
                break;
            case "non-friends":
                model.addAttribute("friendshipStatus", Friendship.FriendshipStatus.NONE.name());
                friends = friendshipService.findNonFriends(currentUser.getId(), page, size);
                listTitle = "Chưa kết bạn";
                break;
            case "sent-requests":
                isSender = true;
                model.addAttribute("friendshipStatus", Friendship.FriendshipStatus.PENDING.name());
                friends = friendshipService.findSentFriendRequests(currentUser.getId(), page, size);
                listTitle = "Lời mời đã gửi";
                break;
            case "received-requests":
                isReceiver = true;
                model.addAttribute("friendshipStatus", Friendship.FriendshipStatus.PENDING.name());
                friends = friendshipService.findReceivedFriendRequests(currentUser.getId(), page, size);
                listTitle = "Lời mời nhận được";
                break;
            case "all":
            default:
                model.addAttribute("friendshipStatus", Friendship.FriendshipStatus.ACCEPTED.name());
                friends = friendshipService.getVisibleFriendList(currentUser, page, size);
                listTitle = "Danh sách bạn bè";
                break;
        }
        model.addAttribute("isReceiver", isReceiver);
        model.addAttribute("isSender", isSender);
        model.addAttribute("friends", friends);
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("targetUserId", targetUserId);
        model.addAttribute("filter", filter);
        model.addAttribute("listTitle", listTitle);
        return "friend/index";
    }

    @GetMapping("/api/friends")
    @ResponseBody
    public Page<FriendDto> getFriends(@RequestParam("page") int page,
                                      @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        User currentUser = userService.getCurrentUser();
        Page<FriendDto> p = friendshipService.getVisibleFriendList(currentUser, page, size);
        return friendshipService.getVisibleFriendList(currentUser, page, size);
    }

    @GetMapping("/api/friends/mutual")
    @ResponseBody
    public Page<FriendDto> getMutualFriends(
            @RequestParam("targetUserId") Long targetUserId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        return friendshipService.findMutualFriends(currentUserId, targetUserId, page, size);
    }

    @GetMapping("/api/friends/non-friends")
    @ResponseBody
    public Page<FriendDto> getNonFriends(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        Page<FriendDto> friendDtos = friendshipService.findNonFriends(currentUserId, page, size);
        return friendshipService.findNonFriends(currentUserId, page, size);
    }

    @GetMapping("/api/friends/sent-requests")
    @ResponseBody
    public Page<FriendDto> getSentFriendRequests(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        return friendshipService.findSentFriendRequests(currentUserId, page, size);
    }

    @GetMapping("/api/friends/received-requests")
    @ResponseBody
    public Page<FriendDto> getReceivedFriendRequests(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        Long currentUserId = currentUser.getId();
        return friendshipService.findReceivedFriendRequests(currentUserId, page, size);
    }


    @PostMapping("/addFriend")
    @ResponseBody
    public ResponseEntity<String> addFriend(@RequestParam String user_name) {
        User friend = userService.getUserByUsername(user_name);
        friendshipService.addFriendship(friend);
        return ResponseEntity.ok("success");
    }

    @PutMapping("/acceptFriend")
    @ResponseBody
    public ResponseEntity<String> acceptFriend(@RequestParam String user_name) {
        User friend = userService.getUserByUsername(user_name);
        friendshipService.acceptFriendship(friend);
        return ResponseEntity.ok("accept success");
    }

    @DeleteMapping("/deleteFriend")
    @ResponseBody
    public ResponseEntity<String> deleteFriend(@RequestParam String user_name) {
        User friend = userService.getUserByUsername(user_name);
        friendshipService.deleteFriendship(friend);
        return ResponseEntity.ok("delete success");
    }

}
