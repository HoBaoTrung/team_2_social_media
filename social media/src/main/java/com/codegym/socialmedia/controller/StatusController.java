package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.status.StatusDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Status;
import com.codegym.socialmedia.service.notification.LikeNotificationService;
import com.codegym.socialmedia.service.status.StatusService;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StatusController {

    @Autowired
   private UserService userService;

    @Autowired
    private StatusService statusService;

    @GetMapping("/news-feed")
    public String getMainFeeds( Model model) {
        // Lấy thông tin người dùng hiện tại
        User currentUser = userService.getCurrentUser();

        // Lấy danh sách status cho feed (public + của bạn bè)
        List<StatusDTO> statuses = statusService.getFeeds();

        // Thêm dữ liệu vào model
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("statuses", statuses);

        return "news-feed"; // Trả về template news-feed.html
    }

    @Autowired
    private LikeNotificationService likeNotificationService;

    @PostMapping("/api/likes/status/{statusId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLikeStatus(@PathVariable Integer statusId) {
        Long userId = userService.getCurrentUser().getId();

        boolean isLiked = statusService.toggleLikeStatus(statusId, userId);
        int likeCount = statusService.getLikeCount(statusId);

        Map<String, Object> response = new HashMap<>();
        response.put("statusId", statusId);
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);

        likeNotificationService.notifyLikeStatusChanged(statusId, likeCount, isLiked);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{statusId}/info")
    public ResponseEntity<Map<String, Object>> getLikeInfo(@PathVariable Integer statusId) {
        Long userId = userService.getCurrentUser().getId();

        boolean isLiked = statusService.toggleLikeStatus(statusId, userId);
        int likeCount = statusService.getLikeCount(statusId);

        Map<String, Object> response = new HashMap<>();
        response.put("isLiked", isLiked);
        response.put("likeCount", likeCount);

        return ResponseEntity.ok(response);
    }
}
