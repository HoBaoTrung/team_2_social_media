package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.repository.PostCommentRepository;
import com.codegym.socialmedia.repository.PostLikeRepository;
import com.codegym.socialmedia.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service để quản lý việc cập nhật count fields trong Post entity
 * Có thể được gọi khi có thay đổi likes, comments, shares
 */
@Service
@Transactional
public class PostCountService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    /**
     * Cập nhật likes count cho một post
     */
    @Async
    public void updateLikesCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            long likesCount = postLikeRepository.countByPost(post);
            post.setLikesCount((int) likesCount);
            postRepository.save(post);
        });
    }

    /**
     * Cập nhật comments count cho một post
     */
    @Async
    public void updateCommentsCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            long commentsCount = postCommentRepository.countByPost(post);
            post.setCommentsCount((int) commentsCount);
            postRepository.save(post);
        });
    }

    /**
     * Cập nhật shares count cho một post
     */
    @Async
    public void updateSharesCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            long sharesCount = postRepository.countByOriginalPostAndIsDeletedFalse(post);
            post.setSharesCount((int) sharesCount);
            postRepository.save(post);
        });
    }

    /**
     * Cập nhật tất cả counts cho một post
     */
    public void updateAllCounts(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            long likesCount = postLikeRepository.countByPost(post);
            long commentsCount = postCommentRepository.countByPost(post);
            long sharesCount = postRepository.countByOriginalPostAndIsDeletedFalse(post);

            post.setLikesCount((int) likesCount);
            post.setCommentsCount((int) commentsCount);
            post.setSharesCount((int) sharesCount);

            postRepository.save(post);
        });
    }

    /**
     * Batch update tất cả counts cho nhiều posts
     */
    @Async
    public void batchUpdateCounts() {
        postRepository.findAll().forEach(post -> {
            updateAllCounts(post.getId());
        });
    }

    /**
     * Increment likes count (alternative to full recalculation)
     */
    public void incrementLikesCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setLikesCount(post.getLikesCount() + 1);
            postRepository.save(post);
        });
    }

    /**
     * Decrement likes count
     */
    public void decrementLikesCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            postRepository.save(post);
        });
    }

    /**
     * Increment comments count
     */
    public void incrementCommentsCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setCommentsCount(post.getCommentsCount() + 1);
            postRepository.save(post);
        });
    }

    /**
     * Decrement comments count
     */
    public void decrementCommentsCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setCommentsCount(Math.max(0, post.getCommentsCount() - 1));
            postRepository.save(post);
        });
    }

    /**
     * Increment shares count
     */
    public void incrementSharesCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setSharesCount(post.getSharesCount() + 1);
            postRepository.save(post);
        });
    }

    /**
     * Decrement shares count
     */
    public void decrementSharesCount(Long postId) {
        postRepository.findById(postId).ifPresent(post -> {
            post.setSharesCount(Math.max(0, post.getSharesCount() - 1));
            postRepository.save(post);
        });
    }
}