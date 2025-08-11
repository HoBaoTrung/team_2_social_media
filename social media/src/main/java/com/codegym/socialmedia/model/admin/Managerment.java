package com.codegym.socialmedia.model.admin;

import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "managements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Managerment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người bị xử lý
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User targetUser;

    // Loại hành động
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ActionType actionType;

    @Column(name = "action_date", nullable = false)
    private LocalDateTime actionDate;

    @Column(columnDefinition = "TEXT")
    private String adminNote;

    public enum ActionType {
        SUSPEND, BAN, WARNING, UNBAN, VERIFY, BLOCK, DELETE_STATUS, DELETE_COMMENT
    }
}
