package com.codegym.socialmedia.service.chat;

import com.codegym.socialmedia.dto.chat.ConversationDto;
import com.codegym.socialmedia.dto.chat.MessageDto;
import com.codegym.socialmedia.dto.chat.SendMessageRequest;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private FriendshipService friendshipService;

    @Override
    public List<ConversationDto> getConversationsForUser(Long userId) {
        // Tạm thời trả về danh sách bạn bè như conversations
        try {
            User currentUser = userRepository.findById(userId).orElse(null);
            if (currentUser == null) return new ArrayList<>();

            // Lấy bạn bè có thể nhắn tin làm conversations
            return friendshipService.findFriendsWithAllowSendMessage(currentUser, 0, 10)
                    .getContent()
                    .stream()
                    .map(friend -> {
                        ConversationDto dto = new ConversationDto();
                        dto.setId(friend.getId().toString());
                        dto.setName(friend.getFirstName() + " " + friend.getLastName());
                        dto.setAvatar(friend.getProfilePicture());
                        dto.setLastMessage("Nhấn để bắt đầu trò chuyện");
                        dto.setTimeAgo("Vừa xong");
                        dto.setOnline(true);
                        dto.setLastMessageTime(LocalDateTime.now());
                        return dto;
                    })
                    .sorted((a, b) -> b.getLastMessageTime().compareTo(a.getLastMessageTime()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<ConversationDto> getOnlineFriends(Long userId) {
        try {
            User currentUser = userRepository.findById(userId).orElse(null);
            if (currentUser == null) return new ArrayList<>();

            // Lấy bạn bè online
            return friendshipService.findFriendsWithAllowSendMessage(currentUser, 0, 20)
                    .getContent()
                    .stream()
                    .map(friend -> {
                        ConversationDto dto = new ConversationDto();
                        dto.setId(friend.getId().toString());
                        dto.setName(friend.getFirstName() + " " + friend.getLastName());
                        dto.setAvatar(friend.getProfilePicture());
                        dto.setOnline(true);
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public MessageDto sendMessage(Long senderId, SendMessageRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // Tạo message DTO
        MessageDto message = new MessageDto();
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setType(request.getType());
        message.setTimestamp(LocalDateTime.now());
        message.setSenderName(sender.getFirstName() + " " + sender.getLastName());
        message.setSenderAvatar(sender.getProfilePicture());
        message.setRead(false);

        return message;
    }

    @Override
    public List<MessageDto> getChatHistory(Long userId1, Long userId2) {
        // Tạm thời trả về empty, sẽ implement khi có bảng message
        return new ArrayList<>();
    }

    @Override
    public void markMessagesAsRead(Long senderId, Long receiverId) {
        // Implement sau khi có bảng message
    }
}