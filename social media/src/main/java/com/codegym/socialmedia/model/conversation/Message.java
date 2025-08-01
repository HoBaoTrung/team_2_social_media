package com.codegym.socialmedia.model.conversation;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
@Entity
@Table(name = "messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;

    @ManyToOne
    @JoinColumn(name = "conversation_id")
    private Conversation conversation;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;

    private String content;
    private String attachmentUrl;
    private String fileName;
    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    private LocalDateTime sentAt;
    private LocalDateTime readAt;

    private Boolean isDeleted;
    private Boolean isRecalled;

    private Integer callDuration;

    @Enumerated(EnumType.STRING)
    private CallStatus callStatus;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<MessageRead> messageReads;

    public enum MessageType {
        TEXT, IMAGE, VIDEO, FILE, AUDIO, CALL
    }

    public enum CallStatus {
        PENDING, MISSED, COMPLETED, FAILED
    }
}
