package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {

    @Query("SELECT pt FROM PostTag pt WHERE pt.post = :post")
    List<PostTag> findByPost(@Param("post") Post post);

    @Query("SELECT pt FROM PostTag pt WHERE pt.taggedUser = :user ORDER BY pt.createdAt DESC")
    Page<PostTag> findByTaggedUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT pt.post FROM PostTag pt WHERE pt.taggedUser = :user AND pt.post.isDeleted = false " +
            "ORDER BY pt.createdAt DESC")
    Page<Post> findPostsUserWasTaggedIn(@Param("user") User user, Pageable pageable);

    boolean existsByPostAndTaggedUser(Post post, User taggedUser);

    void deleteByPostAndTaggedUser(Post post, User taggedUser);
}
