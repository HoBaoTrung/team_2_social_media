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

    // 6. Search posts by user and content
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.isDeleted = false " +
            "AND LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> searchPostsByUserAndContent(@Param("user") User user,
                                           @Param("keyword") String keyword,
                                           Pageable pageable);

    // 7. Find posts for news feed
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsForNewsFeed(@Param("user") User user, Pageable pageable);

    // 8. Find posts by user list (for friends' posts)
    @Query("SELECT p FROM Post p WHERE p.user IN :users AND p.isDeleted = false " +
            "AND (p.privacyLevel = 'PUBLIC' OR p.privacyLevel = 'FRIENDS') " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsByUserIn(@Param("users") List<User> users, Pageable pageable);

    // NEW METHODS FOR EXTENDED FUNCTIONALITY

    // 9. Find posts by post type
    Page<Post> findByUserAndPostTypeAndIsDeletedFalseOrderByCreatedAtDesc(
            User user, Post.PostType postType, Pageable pageable);

    List<Post> findByUserAndPostTypeAndIsDeletedFalseOrderByCreatedAtDesc(
            User user, Post.PostType postType);

    // 10. Find posts by post type and privacy level
    Page<Post> findByUserAndPostTypeAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
            User user, Post.PostType postType, Post.PrivacyLevel privacyLevel, Pageable pageable);

    List<Post> findByUserAndPostTypeAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
            User user, Post.PostType postType, Post.PrivacyLevel privacyLevel);

    // 11. Find posts by multiple post types
    Page<Post> findByUserAndPostTypeInAndIsDeletedFalseOrderByCreatedAtDesc(
            User user, List<Post.PostType> postTypes, Pageable pageable);

    Page<Post> findByUserAndPostTypeInAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
            User user, List<Post.PostType> postTypes, Post.PrivacyLevel privacyLevel, Pageable pageable);

    // 12. Count posts by post type
    long countByUserAndPostTypeAndIsDeletedFalse(User user, Post.PostType postType);

    long countByUserAndPostTypeInAndIsDeletedFalse(User user, List<Post.PostType> postTypes);

    // 13. Find shared posts
    Page<Post> findByOriginalPostAndIsDeletedFalseOrderByCreatedAtDesc(Post originalPost, Pageable pageable);

    // 14. Find posts by location
    Page<Post> findByLocationContainingIgnoreCaseAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
            String location, Post.PrivacyLevel privacyLevel, Pageable pageable);

    // 15. Find posts with tagged users
    @Query("SELECT p FROM Post p WHERE p.taggedUsernames LIKE CONCAT('%\"', :username, '\"%') " +
            "AND p.isDeleted = false AND p.privacyLevel = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Post> findPostsWithTaggedUser(@Param("username") String username, Pageable pageable);

    // 16. Find trending posts (posts with most interactions in recent time)
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND p.privacyLevel = 'PUBLIC' " +
            "AND p.createdAt >= :since ORDER BY (p.likesCount + p.commentsCount) DESC")
    Page<Post> findTrendingPosts(@Param("since") java.time.LocalDateTime since, Pageable pageable);

    // 17. Find posts by feeling
    Page<Post> findByFeelingAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
            String feeling, Post.PrivacyLevel privacyLevel, Pageable pageable);

    // 18. Find posts by activity
    Page<Post> findByActivityAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
            String activity, Post.PrivacyLevel privacyLevel, Pageable pageable);

    // 19. Get recent posts for homepage/feed
    @Query("SELECT p FROM Post p WHERE p.privacyLevel = 'PUBLIC' AND p.isDeleted = false " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findRecentPublicPosts(Pageable pageable);

    // 20. Find posts with media (images or videos)
    @Query("SELECT p FROM Post p WHERE p.user = :user AND p.isDeleted = false " +
            "AND (p.imageUrls IS NOT NULL AND p.imageUrls != '[]' " +
            "OR p.videoUrls IS NOT NULL AND p.videoUrls != '[]') " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findMediaPostsByUser(@Param("user") User user, Pageable pageable);

    // 21. Count shares of a post
    @Query("SELECT COUNT(p) FROM Post p WHERE p.originalPost = :originalPost AND p.isDeleted = false")
    long countByOriginalPostAndIsDeletedFalse(@Param("originalPost") Post originalPost);

    // 22. Advanced search with multiple criteria
    @Query("SELECT p FROM Post p WHERE p.isDeleted = false " +
            "AND (:user IS NULL OR p.user = :user) " +
            "AND (:postType IS NULL OR p.postType = :postType) " +
            "AND (:privacyLevel IS NULL OR p.privacyLevel = :privacyLevel) " +
            "AND (:keyword IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "ORDER BY p.createdAt DESC")
    Page<Post> findPostsWithCriteria(
            @Param("user") User user,
            @Param("postType") Post.PostType postType,
            @Param("privacyLevel") Post.PrivacyLevel privacyLevel,
            @Param("keyword") String keyword,
            @Param("location") String location,
            Pageable pageable
    );
}