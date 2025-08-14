// PostLikeRepository.java
package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    // Tìm like của user cho post cụ thể
    Optional<PostLike> findByPostAndUser(Post post, User user);

    // Kiểm tra user đã like post chưa
    boolean existsByPostAndUser(Post post, User user);

    // Đếm số likes của post
    long countByPost(Post post);

    // Lấy danh sách users đã like post
    @Query("SELECT pl.user FROM PostLike pl WHERE pl.post = :post ORDER BY pl.createdAt DESC")
    List<User> findUsersWhoLikedPost(@Param("post") Post post);

    // Xóa like
    void deleteByPostAndUser(Post post, User user);
}
