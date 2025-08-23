package com.codegym.socialmedia.service.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PostMessage {
    private final SimpMessagingTemplate messagingTemplate;

    public PostMessage(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyLikeStatusChanged(Long statusId, Integer likeCount, boolean isLiked, String userName) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("statusId", statusId);
        payload.put("likeCount", likeCount);
        payload.put("isLiked", isLiked);
        payload.put("userName", userName);

        // Gửi đến tất cả client đang theo dõi status này
        messagingTemplate.convertAndSend("/topic/status/" + statusId + "/likes", payload);
    }

    public void notifyCommentStatusChanged(Long postId, Integer commentCount) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("postId", postId);
        payload.put("commentCount", commentCount);
        messagingTemplate.convertAndSend("/topic/post/" + postId + "/comments", payload);

    }
}