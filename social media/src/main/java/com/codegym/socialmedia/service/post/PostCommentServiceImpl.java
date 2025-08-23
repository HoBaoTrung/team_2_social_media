package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.comment.DisplayCommentDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.*;
import com.codegym.socialmedia.repository.comment.LikeCommentRepository;
import com.codegym.socialmedia.repository.post.PostCommentRepository;
import com.codegym.socialmedia.repository.post.PostRepository;
import com.codegym.socialmedia.service.notification.NotificationService;
import com.codegym.socialmedia.service.notification.PostMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.codegym.socialmedia.dto.comment.DisplayCommentDTO.mapToDTO;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final PostMessage postMessage;
    private final NotificationService notificationService;

    @Override
    public PostComment addComment(Long postId, User user, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setUser(user);
        comment.setContent(content);
        comment.setDeleted(false);
        comment.setCreatedAt(LocalDateTime.now());
        try {
            PostComment savedComment = postCommentRepository.save(comment);
            postMessage.notifyCommentStatusChanged(postId, postCommentRepository.countByPost(post));
            notificationService.notify(user.getId(),post.getUser().getId(), Notification.NotificationType.COMMENT_POST,
                    Notification.ReferenceType.COMMENT, savedComment.getId());
            return savedComment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public PostComment updateComment(Long commentId, User currentUser, String newContent) {
        PostComment comment = postCommentRepository.findByIdAndUser(commentId, currentUser)
                .orElseThrow(() -> new RuntimeException("Comment không tồn tại hoặc bạn không có quyền sửa"));

        comment.setContent(newContent);
        comment.setUpdatedAt(LocalDateTime.now());
        return postCommentRepository.save(comment);
    }

    @Override
    public Page<DisplayCommentDTO> getCommentsByPost(Long postId, User currentUser, int page, int size) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<PostComment> comments = postCommentRepository.findRecentCommentsByPost(post, pageable);

        return comments.map(comment -> mapToDTO(comment, currentUser));
    }

    @Override
    public PostComment deleteComment(Long commentId, User currentUser) {
        PostComment comment = postCommentRepository.findById(commentId).orElse(null);
        if (comment == null || !comment.getUser().getId().equals(currentUser.getId())) {
            return null;
        }

        try {
            Post post = comment.getPost();
            postCommentRepository.delete(comment);
            postMessage.notifyCommentStatusChanged(post.getId(), postCommentRepository.countByPost(post));
            return comment;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public Optional<PostComment> getCommentById(Long commentId) {
        return postCommentRepository.findById(commentId);
    }

    /**
     * Toggle like/unlike comment
     */
    @Override
    public DisplayCommentDTO toggleLikeComment(Long commentId, User currentUser) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        LikeCommentId likeId = new LikeCommentId(currentUser.getId(), commentId);

        Optional<LikeComment> existingLike = likeCommentRepository.findById(likeId);

        if (existingLike.isPresent()) {
            // unlike
            likeCommentRepository.delete(existingLike.get());
        } else {
            // like mới
            LikeComment like = new LikeComment();
            like.setId(likeId);
            like.setUser(currentUser);
            like.setComment(comment);
            likeCommentRepository.save(like);
        }

        // Refresh lại danh sách likedByUsers nếu cần
        comment.setLikedByUsers(likeCommentRepository.findByUserAndComment(currentUser, comment));

        return mapToDTO(comment, currentUser);
    }

}
