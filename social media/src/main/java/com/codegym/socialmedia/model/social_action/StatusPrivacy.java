package com.codegym.socialmedia.model.social_action;
import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "status_privacy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusPrivacy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "status_id")
    private Status status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private boolean canView = true;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
