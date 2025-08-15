package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostShare;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostShareRepository extends JpaRepository<PostShare, Long> {

    Optional<PostShare> findByOriginalPostAndUser(Post originalPost, User user);

    boolean existsByOriginalPostAndUser(Post originalPost, User user);

    long countByOriginalPost(Post originalPost);

    @Query("SELECT ps.user FROM PostShare ps WHERE ps.originalPost = :originalPost ORDER BY ps.createdAt DESC")
    List<User> findUsersWhoSharedPost(@Param("originalPost") Post originalPost);

    @Query("SELECT ps FROM PostShare ps WHERE ps.originalPost = :originalPost ORDER BY ps.createdAt DESC")
    Page<PostShare> findByOriginalPost(@Param("originalPost") Post originalPost, Pageable pageable);

    @Query("SELECT ps FROM PostShare ps WHERE ps.user = :user ORDER BY ps.createdAt DESC")
    Page<PostShare> findByUser(@Param("user") User user, Pageable pageable);
}
