package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.post.PostCreateDto;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.model.PrivacyLevel;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Notification;
import com.codegym.socialmedia.repository.NotificationRepository;
import com.codegym.socialmedia.service.post.PostService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CommonController {

    @Autowired
   private UserService userService;

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
        model.addAttribute("privacyLevels", PrivacyLevel.values());

        return "news-feed";
    }

}
