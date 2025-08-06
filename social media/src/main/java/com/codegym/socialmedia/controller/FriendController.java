package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class FriendController {
    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserService userService;

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
