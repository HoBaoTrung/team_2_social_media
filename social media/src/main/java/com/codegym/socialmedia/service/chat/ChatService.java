package com.codegym.socialmedia.service.chat;

import com.codegym.socialmedia.dto.chat.ConversationDto;
import com.codegym.socialmedia.dto.chat.MessageDto;
import com.codegym.socialmedia.dto.chat.SendMessageRequest;
import java.util.List;

public interface ChatService {
    List<ConversationDto> getConversationsForUser(Long userId);
    List<MessageDto> getChatHistory(Long userId1, Long userId2);
    MessageDto sendMessage(Long senderId, SendMessageRequest request);
    List<ConversationDto> getOnlineFriends(Long userId);
    void markMessagesAsRead(Long senderId, Long receiverId);
}