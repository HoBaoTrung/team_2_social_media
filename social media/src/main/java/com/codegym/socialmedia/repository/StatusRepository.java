// StatusRepository.java - Fixed version với query đúng
package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.social_action.Status;
import com.codegym.socialmedia.model.account.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatusRepository extends JpaRepository<Status, Long> {

    // ===== BASIC QUERIES =====
    Page<Status> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Status> findByUserAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
            User user, Status.PrivacyLevel privacyLevel, Pageable pageable);

    Optional<Status> findByIdAndIsDeletedFalse(Long id);

    long countByUserAndIsDeletedFalse(User user);

    List<Status> findByUserAndIsPinnedTrueAndIsDeletedFalseOrderByCreatedAtDesc(User user);

    /**
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND " +
            "s.privacyLevel = 'PUBLIC' AND " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findByContentContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    /**
     */
    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findByUserAndContentContainingIgnoreCase(
            @Param("user") User user, @Param("query") String query, Pageable pageable);

    /**
     */
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')) " +

    // ===== NEWSFEED QUERIES =====
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND " +
            "((s.user IN :friends AND s.privacyLevel IN ('PUBLIC', 'FRIENDS')) OR " +
            "(s.user NOT IN :friends AND s.privacyLevel = 'PUBLIC')) " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findNewsFeedStatuses(@Param("friends") List<User> friends, Pageable pageable);

    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND s.privacyLevel = 'PUBLIC' " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findPublicStatuses(Pageable pageable);

    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND " +
            "(s.user = :currentUser OR " +
            "(s.user IN :friends AND s.privacyLevel IN ('PUBLIC', 'FRIENDS')) OR " +
            "s.privacyLevel = 'PUBLIC') " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findNewsFeedForUser(
            @Param("currentUser") User currentUser,
            @Param("friends") List<User> friends,
            Pageable pageable);

    // ===== STATISTICS QUERIES =====
    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    Page<Status> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND s.privacyLevel = 'PUBLIC' " +
            "ORDER BY s.likeCount DESC, s.createdAt DESC")
    Page<Status> findTopLikedStatuses(Pageable pageable);

    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND s.privacyLevel = 'PUBLIC' " +
            "AND s.createdAt >= :since ORDER BY (s.likeCount + s.commentCount + s.shareCount) DESC")
    Page<Status> findTrendingStatuses(@Param("since") LocalDateTime since, Pageable pageable);

    // ===== CONTENT TYPE QUERIES =====
    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "s.imageUrls IS NOT NULL AND s.imageUrls != '' " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findStatusesWithImagesByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "s.videoUrl IS NOT NULL AND s.videoUrl != '' " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findStatusesWithVideosByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "(s.imageUrls IS NULL OR s.imageUrls = '') AND " +
            "(s.videoUrl IS NULL OR s.videoUrl = '') " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findTextOnlyStatusesByUser(@Param("user") User user, Pageable pageable);
}