package com.codegym.socialmedia.model.admin;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "admin_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name; // SUPER_ADMIN, ADMIN, MODERATOR

    private String description;

    @OneToMany(mappedBy = "role")
    private List<Admin> admins;

}
