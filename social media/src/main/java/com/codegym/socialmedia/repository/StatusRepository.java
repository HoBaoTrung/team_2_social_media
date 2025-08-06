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

    /**
     * Tìm tất cả status của một user (cho trang cá nhân)
     */
    Page<Status> findByUserAndIsDeletedFalseOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Tìm status public của một user (cho khách)
     */
    Page<Status> findByUserAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
            User user, Status.PrivacyLevel privacyLevel, Pageable pageable);

    /**
     * Tìm status theo ID và kiểm tra không bị xóa
     */
    Optional<Status> findByIdAndIsDeletedFalse(Long id);

    /**
     * Đếm số status của user
     */
    long countByUserAndIsDeletedFalse(User user);

    /**
     * Tìm status được pin của user
     */
    List<Status> findByUserAndIsPinnedTrueAndIsDeletedFalseOrderByCreatedAtDesc(User user);

    // ===== SEARCH QUERIES =====

    /**
     * Tìm kiếm status theo nội dung (toàn bộ hệ thống)
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND " +
            "s.privacyLevel = 'PUBLIC' AND " +
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findByContentContainingIgnoreCase(@Param("query") String query, Pageable pageable);

    /**
     * Tìm kiếm status của user cụ thể theo nội dung
     */
    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findByUserAndContentContainingIgnoreCase(
            @Param("user") User user, @Param("query") String query, Pageable pageable);

    /**
     * Tìm kiếm status với privacy level cụ thể
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND " +
            "s.privacyLevel = :privacyLevel AND " +
            "LOWER(s.content) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findByPrivacyLevelAndContentContainingIgnoreCase(
            @Param("privacyLevel") Status.PrivacyLevel privacyLevel,
            @Param("query") String query,
            Pageable pageable);

    // ===== NEWSFEED QUERIES =====

    /**
     * Lấy newsfeed - status của bạn bè và public
     * TODO: Cần implement logic friendship để lấy đúng danh sách friends
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND " +
            "((s.user IN :friends AND s.privacyLevel IN ('PUBLIC', 'FRIENDS')) OR " +
            "(s.user NOT IN :friends AND s.privacyLevel = 'PUBLIC')) " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findNewsFeedStatuses(@Param("friends") List<User> friends, Pageable pageable);

    /**
     * Lấy newsfeed cho khách (chỉ public)
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND s.privacyLevel = 'PUBLIC' " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findPublicStatuses(Pageable pageable);

    /**
     * Lấy newsfeed của user đã đăng nhập (bao gồm cả post của chính mình)
     */
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

    /**
     * Tìm status trong khoảng thời gian
     */
    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    Page<Status> findByUserAndDateRange(
            @Param("user") User user,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Top status có nhiều like nhất
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND s.privacyLevel = 'PUBLIC' " +
            "ORDER BY s.likeCount DESC, s.createdAt DESC")
    Page<Status> findTopLikedStatuses(Pageable pageable);

    /**
     * Status trending (nhiều tương tác gần đây)
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND s.privacyLevel = 'PUBLIC' " +
            "AND s.createdAt >= :since ORDER BY (s.likeCount + s.commentCount + s.shareCount) DESC")
    Page<Status> findTrendingStatuses(@Param("since") LocalDateTime since, Pageable pageable);

    // ===== CONTENT TYPE QUERIES =====

    /**
     * Tìm status có ảnh của user
     */
    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "s.imageUrls IS NOT NULL AND s.imageUrls != '' " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findStatusesWithImagesByUser(@Param("user") User user, Pageable pageable);

    /**
     * Tìm status có video của user
     */
    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "s.videoUrl IS NOT NULL AND s.videoUrl != '' " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findStatusesWithVideosByUser(@Param("user") User user, Pageable pageable);

    /**
     * Tìm status text only của user
     */
    @Query("SELECT s FROM Status s WHERE s.user = :user AND s.isDeleted = false AND " +
            "(s.imageUrls IS NULL OR s.imageUrls = '') AND " +
            "(s.videoUrl IS NULL OR s.videoUrl = '') " +
            "ORDER BY s.createdAt DESC")
    Page<Status> findTextOnlyStatusesByUser(@Param("user") User user, Pageable pageable);

    // ===== ADVANCED QUERIES =====

    /**
     * Tìm status được chia sẻ nhiều nhất trong khoảng thời gian
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND s.privacyLevel = 'PUBLIC' " +
            "AND s.createdAt >= :since AND s.shareCount > 0 " +
            "ORDER BY s.shareCount DESC")
    Page<Status> findMostSharedStatuses(@Param("since") LocalDateTime since, Pageable pageable);

    /**
     * Tìm status có nhiều bình luận nhất
     */
    @Query("SELECT s FROM Status s WHERE s.isDeleted = false AND s.privacyLevel = 'PUBLIC' " +
            "AND s.commentCount > 0 " +
            "ORDER BY s.commentCount DESC")
    Page<Status> findMostCommentedStatuses(Pageable pageable);

    /**
     * Lấy thống kê status theo loại của user
     */
    @Query("SELECT s.statusType, COUNT(s) FROM Status s " +
            "WHERE s.user = :user AND s.isDeleted = false " +
            "GROUP BY s.statusType")
    List<Object[]> getStatusTypeStatsByUser(@Param("user") User user);

    /**
     * Lấy thống kê privacy level của user
     */
    @Query("SELECT s.privacyLevel, COUNT(s) FROM Status s " +
            "WHERE s.user = :user AND s.isDeleted = false " +
            "GROUP BY s.privacyLevel")
    List<Object[]> getPrivacyLevelStatsByUser(@Param("user") User user);
}