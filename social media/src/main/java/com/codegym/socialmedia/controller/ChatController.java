package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.chat.ConversationDto;
import com.codegym.socialmedia.dto.chat.MessageDto;
import com.codegym.socialmedia.dto.chat.SendMessageRequest;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.chat.ChatService;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.user.UserService;  // ← Import này
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChatController {

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;  // ← Thêm dòng này

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    // API lấy danh sách cuộc trò chuyện cho dropdown
    @GetMapping("/api/conversations")
    @ResponseBody
    public ResponseEntity<List<ConversationDto>> getConversations() {
        User currentUser = userService.getCurrentUser();
        List<ConversationDto> conversations = chatService.getConversationsForUser(currentUser.getId());
        return ResponseEntity.ok(conversations);
    }

    // API lấy bạn bè online cho sidebar
    @GetMapping("/api/chat/online-friends")
    @ResponseBody
    public ResponseEntity<List<ConversationDto>> getOnlineFriends() {
        User currentUser = userService.getCurrentUser();
        List<ConversationDto> onlineFriends = chatService.getOnlineFriends(currentUser.getId());
        return ResponseEntity.ok(onlineFriends);
    }

    // API gửi tin nhắn
    @PostMapping("/api/chat/send-message")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sendMessage(@RequestBody SendMessageRequest request) {
        User currentUser = userService.getCurrentUser();
        Map<String, Object> response = new HashMap<>();

        try {
            MessageDto message = chatService.sendMessage(currentUser.getId(), request);

            // Gửi real-time notification
            messagingTemplate.convertAndSendToUser(
                    request.getReceiverId().toString(),
                    "/queue/messages",
                    message
            );

            response.put("success", true);
            response.put("message", message);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    // Existing method giữ nguyên
    @GetMapping("/chat-window")
    public String getChatWindow(@RequestParam String chatId,
                                @RequestParam String chatName,
                                @RequestParam String chatType,
                                @RequestParam String avatar,
                                Model model) {
        model.addAttribute("chatId", chatId);
        model.addAttribute("chatName", chatName);
        model.addAttribute("chatType", chatType);
        model.addAttribute("avatar", avatar);
        return "fragments/chat-window :: chatWindow";
    }

}