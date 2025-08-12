package com.codegym.socialmedia.service.notification;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LikeNotificationService {
    private final SimpMessagingTemplate messagingTemplate;

    public LikeNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyLikeStatusChanged(Integer statusId, Integer likeCount, boolean isLiked) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("statusId", statusId);
        payload.put("likeCount", likeCount);
        payload.put("isLiked", isLiked);

        // Gửi đến tất cả client đang theo dõi status này
        messagingTemplate.convertAndSend("/topic/status/" + statusId + "/likes", payload);
    }
}