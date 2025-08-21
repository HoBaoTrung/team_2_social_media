package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.comment.DisplayCommentDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostCommentService {
    PostComment createComment(Post post, User user, String content);
    void deleteComment(Long commentId, User user);
    Page<PostComment> getCommentsByPost(Post post, Pageable pageable);
    Page<DisplayCommentDTO> getDisplayCommentsByPost(Post post, Pageable pageable);
    PostComment getCommentById(Long commentId);
}