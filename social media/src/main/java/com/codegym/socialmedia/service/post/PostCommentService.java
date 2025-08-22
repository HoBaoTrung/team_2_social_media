package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.comment.DisplayCommentDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.PostComment;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface PostCommentService {

    PostComment addComment(Long postId, User user, String content);

    Page<DisplayCommentDTO> getCommentsByPost(Long postId, User currentUser, int page, int size);
    // Thêm method update comment
    PostComment updateComment(Long commentId, User currentUser, String newContent);

    DisplayCommentDTO mapToDTO(PostComment updated, User currentUser);

    Optional<PostComment> DeleteCommentAndReturn(Long commentId, User currentUser);
}

