package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // BASIC methods - không có complex queries

    // 1. Find posts by user
    List<Post> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user);

    // 2. Find posts by user with pagination
    Page<Post> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    // 3. Find public posts by user
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.privacyLevel = 'PUBLIC' AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findPublicPostsByUser(@Param("user") User user, Pageable pageable);

    // 4. Count posts by user
    long countByUserAndIsDeletedFalse(User user);

    // 5. Find by ID and user
    Optional<Post> findByIdAndUser(Long id, User user);

    // ❌ TẠM THỜI LOẠI BỎ tất cả complex queries để tránh lỗi validation
    // - searchPostsByUserAndContent
    // - findPostsForNewsFeed
    // - findFriendsPosts
}