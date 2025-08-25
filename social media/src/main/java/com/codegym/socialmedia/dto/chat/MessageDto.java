package com.codegym.socialmedia.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private String content;
    private String type;
    private LocalDateTime timestamp;
    private boolean isRead;
}