package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.comment.DisplayCommentDTO;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.LikeCommentId;
import com.codegym.socialmedia.model.social_action.LikePostId;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.codegym.socialmedia.repository.comment.CommentLikeRepository;
import com.codegym.socialmedia.repository.post.PostCommentRepository;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
public class PostCommentServiceImpl implements PostCommentService {

    @Autowired
    private PostCommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository likeRepository;

    @Autowired
    private  UserService userService;

    @Override
    public PostComment createComment(Post post, User user, String content) {
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long commentId, User user) {
        PostComment comment = commentRepository.findByIdAndUser(commentId, user)
                .orElseThrow(() -> new RuntimeException("Comment not found or access denied"));

        comment.setDeleted(true);
        commentRepository.save(comment);
    }


    @Override
    public Page<PostComment> getCommentsByPost(Post post, Pageable pageable) {
        return commentRepository.findByPostOrderByCreatedAtAsc(post, pageable);
    }


    @Override
    public Page<DisplayCommentDTO> getDisplayCommentsByPost(Post post, Pageable pageable) {
        Page<PostComment> comments = getCommentsByPost(post, pageable);

        return comments.map(comment ->
                convertToDisplayDto(comment, userService.getCurrentUser())
        );

    }

    // Helper methods
    private DisplayCommentDTO convertToDisplayDto(PostComment comment, User currentUser) {
        LikeCommentId likeCommentId = new LikeCommentId();
        likeCommentId.setCommentId(comment.getId());
        likeCommentId.setUserId(currentUser.getId());
        boolean isLiked = currentUser != null &&
                likeRepository.findById(likeCommentId).isPresent();

        boolean canEdit = currentUser != null &&
                comment.getUser().getId().equals(currentUser.getId());

        boolean canDelete = canEdit;

        DisplayCommentDTO dto = new DisplayCommentDTO(comment, isLiked);
        dto.setLikeCount(likeRepository.countByComment(comment));
       dto.setCanEdit(canEdit);
       dto.setCanDeleted(canDelete);
        return dto;
    }


}
