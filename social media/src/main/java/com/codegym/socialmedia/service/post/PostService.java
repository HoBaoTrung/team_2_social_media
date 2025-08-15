package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.dto.post.PostCreateDto;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.dto.post.PostUpdateDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PostService {

    // CRUD Operations
    Post createPost(PostCreateDto dto, User user);
    Post updatePost(Long postId, PostUpdateDto dto, User user);
    void deletePost(Long postId, User user);
    PostDisplayDto getPostById(Long postId, User currentUser);

    // Get posts for different contexts
    Page<PostDisplayDto> getPostsForNewsFeed(User currentUser, Pageable pageable);
    Page<PostDisplayDto> getPostsByUser(User targetUser, User currentUser, Pageable pageable);
    Page<PostDisplayDto> getPublicPostsByUser(User targetUser, Pageable pageable);
    Page<PostDisplayDto> searchUserPosts(User user, String keyword, Pageable pageable);

    // Media posts
    Page<PostDisplayDto> getMediaPostsByUser(User targetUser, User currentUser, Pageable pageable);
    List<Map<String, String>> getUserPhotos(User targetUser, User currentUser);
    List<Map<String, String>> getUserVideos(User targetUser, User currentUser);

    // Post interactions
    boolean toggleLike(Long postId, User user);
    boolean toggleReaction(Long postId, User user, String reactionType);
    List<User> getUsersWhoLiked(Long postId);

    // Save/bookmark functionality
    boolean savePost(Long postId, User user);
    boolean unsavePost(Long postId, User user);
    Page<PostDisplayDto> getSavedPosts(User user, Pageable pageable);

    // Record view (for analytics)
    void recordView(Long postId, User user);

    // Share functionality
    Post sharePost(Long originalPostId, String shareText, User user, Post.PrivacyLevel privacyLevel);
    Page<PostDisplayDto> getSharedPosts(Long originalPostId, Pageable pageable);

    // Privacy and permissions
    boolean canUserViewPost(Post post, User viewer);
    boolean canUserEditPost(Post post, User user);
    boolean canUserDeletePost(Post post, User user);

    // Statistics
    long countUserPosts(User user);
    long countUserMediaPosts(User user);
    Map<String, Long> getPostStatistics(User user);

    // Advanced features
    Page<PostDisplayDto> getPostsByType(User targetUser, User currentUser, Post.PostType postType, Pageable pageable);
    Page<PostDisplayDto> getPostsByLocation(String location, User currentUser, Pageable pageable);
    Page<PostDisplayDto> getPostsWithTag(String username, User currentUser, Pageable pageable);
}