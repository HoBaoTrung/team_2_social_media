package com.codegym.socialmedia.model.admin;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer adminId;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Column(length = 100)
    private String fullName;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private AdminRole role;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private Boolean isActive = true;

    private LocalDateTime lastLogin;

//    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
//    private List<Managerment> managedAccounts;

        @OneToMany(mappedBy = "resolvedBy")
    private List<ModerationLog> managedAccounts;

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private List<Stat> statistics;

}
