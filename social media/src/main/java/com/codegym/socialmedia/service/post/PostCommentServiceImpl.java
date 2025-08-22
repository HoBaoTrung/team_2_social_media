package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.comment.DisplayCommentDTO;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.codegym.socialmedia.repository.post.PostCommentRepository;
import com.codegym.socialmedia.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;

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

        return postCommentRepository.save(comment);
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

    /**
     * Mapping entity -> DTO, có kiểm tra quyền sửa/xóa và likedByCurrentUser
     */
    public DisplayCommentDTO mapToDTO(PostComment comment, User currentUser) {
        DisplayCommentDTO dto = new DisplayCommentDTO();
        dto.setCommentId(comment.getId());
        dto.setComment(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUserFullName(comment.getUser().getFirstName() + " " + comment.getUser().getLastName());
        dto.setUsername(comment.getUser().getUsername());
        dto.setUserAvatarUrl(comment.getUser().getProfilePicture());

        // Quyền edit/delete: chỉ cho user comment đó
        dto.setCanEdit(comment.getUser().getId().equals(currentUser.getId()));
        dto.setCanDeleted(comment.getUser().getId().equals(currentUser.getId()));

        // likedByCurrentUser nếu bạn có bảng like comment, ví dụ:
        // dto.setLikedByCurrentUser(comment.getLikes().contains(currentUser));
        // Nếu chưa có, tạm set false
        dto.setLikedByCurrentUser(false);

        // Nếu bạn có số like
        // dto.setLikeCount(comment.getLikes().size());
        dto.setLikeCount(0);

        return dto;
    }
    @Override
    public Optional<PostComment> DeleteCommentAndReturn(Long commentId, User currentUser) {
        Optional<PostComment> optionalComment = postCommentRepository.findById(commentId);
        if (optionalComment.isEmpty()) return Optional.empty();

        PostComment comment = optionalComment.get();

        // Chỉ owner mới xóa
        if (!comment.getUser().getId().equals(currentUser.getId())) {
            return Optional.empty();
        }

        postCommentRepository.delete(comment);
        return Optional.of(comment);
    }

}

