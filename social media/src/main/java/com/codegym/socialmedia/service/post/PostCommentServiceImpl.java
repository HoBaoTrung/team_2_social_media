package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostComment;
import com.codegym.socialmedia.repository.post.PostCommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostCommentServiceImpl implements PostCommentService {

    @Autowired
    private PostCommentRepository commentRepository;

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



}
