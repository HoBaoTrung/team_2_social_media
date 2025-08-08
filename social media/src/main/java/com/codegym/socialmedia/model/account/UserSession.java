package com.codegym.socialmedia.model.account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 255)
    private String sessionToken;

    @Column(length = 45)
    private String ipAddress;

    @Lob
    private String userAgent;

    @Column(length = 255)
    private String deviceInfo;

    @Enumerated(EnumType.STRING)
    private LoginMethod loginMethod;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime lastActivity;

    private boolean isActive;

    public enum LoginMethod {
        WEB, MOBILE, TABLET
    }
}
