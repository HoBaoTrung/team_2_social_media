package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostReaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {

    Optional<PostReaction> findByPostAndUser(Post post, User user);

    boolean existsByPostAndUser(Post post, User user);

    long countByPost(Post post);

    @Query("SELECT pr.user FROM PostReaction pr WHERE pr.post = :post ORDER BY pr.createdAt DESC")
    List<User> findUsersWhoReactedToPost(@Param("post") Post post);

    @Query("SELECT pr FROM PostReaction pr WHERE pr.post = :post AND pr.reactionType = :reactionType " +
            "ORDER BY pr.createdAt DESC")
    Page<PostReaction> findByPostAndReactionType(@Param("post") Post post,
                                                 @Param("reactionType") PostReaction.ReactionType reactionType,
                                                 Pageable pageable);

    @Query("SELECT pr.reactionType, COUNT(pr) FROM PostReaction pr WHERE pr.post = :post " +
            "GROUP BY pr.reactionType ORDER BY COUNT(pr) DESC")
    List<Object[]> getReactionCountsByPost(@Param("post") Post post);

    void deleteByPostAndUser(Post post, User user);
}
