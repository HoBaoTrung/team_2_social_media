package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.post.CommentCreateDto;
import com.codegym.socialmedia.dto.post.CommentUpdateDto;
import com.codegym.socialmedia.dto.post.PostCommentDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PostCommentService {

    // ===== CRUD OPERATIONS =====
    PostComment createComment(Post post, User user, String content);
    PostComment updateComment(Long commentId, CommentUpdateDto dto, User user);
    void deleteComment(Long commentId, User user);

    // ===== GET COMMENTS =====
    Page<PostCommentDto> getCommentsByPost(Post post, Pageable pageable);
    long countCommentsByPost(Post post);

    // ===== COMMENT INTERACTIONS =====
    boolean toggleCommentLike(Long commentId, User user);
    List<User> getUsersWhoLikedComment(Long commentId);

    // ===== COMMENT REPLIES =====
    Page<PostCommentDto> getRepliesByComment(Long commentId, Pageable pageable);
    PostComment replyToComment(Long parentCommentId, User user, String content);

    // ===== UTILITY =====
    PostCommentDto convertToCommentDto(PostComment comment, User currentUser);
    boolean canUserEditComment(PostComment comment, User user);
    boolean canUserDeleteComment(PostComment comment, User user);
}