package com.codegym.socialmedia.repository.post;

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

    // 3. Find public posts by user - THÊM METHOD NÀY
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.privacyLevel = 'PUBLIC' AND p.isDeleted = false ORDER BY p.createdAt DESC")
    Page<Post> findPublicPostsByUser(@Param("user") User user, Pageable pageable);

    // 4. Count posts by user
    long countByUserAndIsDeletedFalse(User user);

    // 5. Find by ID and user
    Optional<Post> findByIdAndUser(Long id, User user);

    // 6. Search posts by user and content - THÊM METHOD NÀY
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.isDeleted = false " +
            "AND LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> searchPostsByUserAndContent(@Param("user") User user,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    // 7. Find posts for news feed - THÊM METHOD NÀY
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsForNewsFeed(@Param("user") User user, Pageable pageable);

    @Query("""
    SELECT p FROM Post p
    WHERE p.isDeleted = FALSE AND (
           p.privacyLevel = 'PUBLIC'
        OR (p.privacyLevel = 'FRIENDS' AND 
            EXISTS (SELECT f FROM Friendship f 
                    WHERE f.status = 'ACCEPTED'
                      AND (f.requester.id = :currentUser OR f.addressee.id = :currentUser)
                   ))
        OR (p.user.id = :currentUser)
    )
    ORDER BY p.createdAt DESC
    """)
    Page<Post> findVisiblePosts(@Param("currentUser") Long currentUser, Pageable pageable);



    // 8. Find posts by user list (for friends' posts) - THÊM METHOD NÀY
    @Query("SELECT p FROM Post p WHERE p.user IN :users AND p.isDeleted = false " +
            "AND (p.privacyLevel = 'PUBLIC' OR p.privacyLevel = 'FRIENDS') " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsByUserIn(@Param("users") List<User> users, Pageable pageable);
}