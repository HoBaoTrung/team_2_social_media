package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    long countByCreatedAt(LocalDate date);
    long countByCreatedAtAfter(LocalDate date);

}