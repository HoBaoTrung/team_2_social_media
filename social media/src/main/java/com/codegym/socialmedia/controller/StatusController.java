package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.post.PostCreateDto;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.service.notification.LikeNotificationService;
import com.codegym.socialmedia.service.post.PostService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class StatusController {

    @Autowired
   private UserService userService;

//    @Autowired
//    private StatusService statusService;

    @Autowired
    private PostService postService;

    @GetMapping("/news-feed")
    public String postsPage(Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostDisplayDto> posts = postService.getPostsForNewsFeed(currentUser, pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("postCreateDto", new PostCreateDto());
        model.addAttribute("privacyLevels", Post.PrivacyLevel.values());

        return "news-feed";
    }


    @Autowired
    private LikeNotificationService likeNotificationService;

    @PostMapping("/api/likes/status/{statusId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLikeStatus(@PathVariable Long statusId) {
        User user = userService.getCurrentUser();
        Post post = postService.getPostById(statusId);
        boolean isLiked = postService.toggleLike(statusId, user);
        int likeCount = postService.getLikeCount(post);

        Map<String, Object> response = new HashMap<>();
        response.put("statusId", statusId);
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }

//    @GetMapping("/status/{statusId}/info")
//    public ResponseEntity<Map<String, Object>> getLikeInfo(@PathVariable Integer statusId) {
//        Long userId = userService.getCurrentUser().getId();
//
//        boolean isLiked = statusService.toggleLikeStatus(statusId, userId);
//        int likeCount = statusService.getLikeCount(statusId);
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("isLiked", isLiked);
//        response.put("likeCount", likeCount);
//
//        return ResponseEntity.ok(response);
//    }
}
