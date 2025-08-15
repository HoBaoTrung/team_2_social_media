package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {

    boolean existsByPostAndUserAndViewedAtAfter(Post post, User user, LocalDateTime afterTime);

    long countByPost(Post post);

    long countDistinctUserByPost(Post post);

    @Query("SELECT COUNT(DISTINCT pv.ipAddress) FROM PostView pv WHERE pv.post = :post")
    long countUniqueViewsByPost(@Param("post") Post post);

    @Query("SELECT pv FROM PostView pv WHERE pv.post = :post AND pv.user IS NOT NULL " +
            "ORDER BY pv.viewedAt DESC")
    Page<PostView> findByPost(@Param("post") Post post, Pageable pageable);

    // Analytics - views by time period
    @Query("SELECT DATE(pv.viewedAt), COUNT(pv) FROM PostView pv WHERE pv.post = :post " +
            "AND pv.viewedAt >= :fromDate GROUP BY DATE(pv.viewedAt) ORDER BY DATE(pv.viewedAt)")
    List<Object[]> getViewStatsByPost(@Param("post") Post post, @Param("fromDate") LocalDateTime fromDate);
}