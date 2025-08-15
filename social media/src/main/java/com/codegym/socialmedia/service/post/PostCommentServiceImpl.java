package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.post.CommentUpdateDto;
import com.codegym.socialmedia.dto.post.PostCommentDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.codegym.socialmedia.repository.PostCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostCommentServiceImpl implements PostCommentService {

    @Autowired
    private PostCommentRepository commentRepository;

    // ===== CRUD OPERATIONS =====

    @Override
    public PostComment createComment(Post post, User user, String content) {
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        return commentRepository.save(comment);
    }

    @Override
    public PostComment updateComment(Long commentId, CommentUpdateDto dto, User user) {
        PostComment comment = commentRepository.findByIdAndUser(commentId, user)
                .orElseThrow(() -> new RuntimeException("Comment not found or access denied"));

        comment.setContent(dto.getContent());
        return commentRepository.save(comment);
    }

    @Override
    public void deleteComment(Long commentId, User user) {
        PostComment comment = commentRepository.findByIdAndUser(commentId, user)
                .orElseThrow(() -> new RuntimeException("Comment not found or access denied"));

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    // ===== GET COMMENTS =====

    @Override
    public Page<PostCommentDto> getCommentsByPost(Post post, Pageable pageable) {
        Page<PostComment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post, pageable);

        List<PostCommentDto> commentDtos = comments.getContent().stream()
                .map(comment -> convertToCommentDto(comment, null))
                .collect(Collectors.toList());

        return new PageImpl<>(commentDtos, pageable, comments.getTotalElements());
    }

    // Raw version for internal use - RENAME để tránh conflict
    public Page<PostComment> getRawCommentsByPost(Post post, Pageable pageable) {
        return commentRepository.findByPostOrderByCreatedAtAsc(post, pageable);
    }

    @Override
    public long countCommentsByPost(Post post) {
        return commentRepository.countByPost(post);
    }

    // ===== COMMENT INTERACTIONS - Stub implementations =====

    @Override
    public boolean toggleCommentLike(Long commentId, User user) {
        // TODO: Implement comment like functionality
        return true;
    }

    @Override
    public List<User> getUsersWhoLikedComment(Long commentId) {
        // TODO: Implement comment likes tracking
        return List.of();
    }

    // ===== COMMENT REPLIES - Stub implementations =====

    @Override
    public Page<PostCommentDto> getRepliesByComment(Long commentId, Pageable pageable) {
        // TODO: Implement comment replies
        return Page.empty();
    }

    @Override
    public PostComment replyToComment(Long parentCommentId, User user, String content) {
        // TODO: Implement comment replies
        PostComment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        PostComment reply = new PostComment();
        reply.setPost(parentComment.getPost());
        reply.setUser(user);
        reply.setContent(content);
        // TODO: Set parent comment reference

        return commentRepository.save(reply);
    }

    // ===== UTILITY =====

    @Override
    public PostCommentDto convertToCommentDto(PostComment comment, User currentUser) {
        PostCommentDto dto = new PostCommentDto();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());

        // User info
        dto.setUserId(comment.getUser().getId());
        dto.setUsername(comment.getUser().getUsername());
        dto.setUserFullName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        dto.setUserAvatarUrl(comment.getUser().getProfilePicture());

        // Permissions
        boolean canEdit = currentUser != null &&
                comment.getUser().getId().equals(currentUser.getId());
        dto.setCanEdit(canEdit);
        dto.setCanDelete(canEdit);

        return dto;
    }

    @Override
    public boolean canUserEditComment(PostComment comment, User user) {
        return user != null && comment.getUser().getId().equals(user.getId());
    }

    @Override
    public boolean canUserDeleteComment(PostComment comment, User user) {
        return canUserEditComment(comment, user);
    }
}