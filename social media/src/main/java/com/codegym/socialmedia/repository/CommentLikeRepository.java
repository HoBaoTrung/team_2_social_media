package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.CommentLike;
import com.codegym.socialmedia.model.social_action.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentAndUser(PostComment comment, User user);

    boolean existsByCommentAndUser(PostComment comment, User user);

    long countByComment(PostComment comment);

    @Query("SELECT cl.user FROM CommentLike cl WHERE cl.comment = :comment ORDER BY cl.createdAt DESC")
    List<User> findUsersWhoLikedComment(@Param("comment") PostComment comment);

    void deleteByCommentAndUser(PostComment comment, User user);
}
