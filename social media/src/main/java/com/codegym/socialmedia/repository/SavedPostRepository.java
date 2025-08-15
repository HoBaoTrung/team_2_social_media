package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.SavedPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {

    Optional<SavedPost> findByUserAndPost(User user, Post post);

    boolean existsByUserAndPost(User user, Post post);

    @Query("SELECT sp FROM SavedPost sp WHERE sp.user = :user ORDER BY sp.createdAt DESC")
    Page<SavedPost> findByUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT sp FROM SavedPost sp WHERE sp.user = :user AND sp.collectionName = :collectionName " +
            "ORDER BY sp.createdAt DESC")
    Page<SavedPost> findByUserAndCollection(@Param("user") User user,
                                            @Param("collectionName") String collectionName,
                                            Pageable pageable);

    @Query("SELECT DISTINCT sp.collectionName FROM SavedPost sp WHERE sp.user = :user")
    List<String> findCollectionNamesByUser(@Param("user") User user);

    long countByUser(User user);

    void deleteByUserAndPost(User user, Post post);
}
