package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {
    private String id;
    private String name;
    private String avatar;
    private String lastMessage;
    private String timeAgo;
    private boolean isOnline;
    private boolean hasUnread;
    private int unreadCount;
    private LocalDateTime lastMessageTime;
}