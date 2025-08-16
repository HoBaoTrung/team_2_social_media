package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.component.CloudinaryService;
import com.codegym.socialmedia.dto.post.PostCreateDto;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.dto.post.PostUpdateDto;
import com.codegym.socialmedia.model.PrivacyLevel;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.LikePost;
import com.codegym.socialmedia.model.social_action.LikePostId;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.repository.FriendshipRepository;
import com.codegym.socialmedia.repository.post.PostCommentRepository;
import com.codegym.socialmedia.repository.post.PostLikeRepository;
import com.codegym.socialmedia.repository.post.PostRepository;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.notification.LikeNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.codegym.socialmedia.component.PrivacyUtils.canView;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LikeNotificationService likeNotificationService;

    @Override
    public Post createPost(PostCreateDto dto, User user) {
        Post post = new Post();
        post.setUser(user);
        post.setContent(dto.getContent());
        post.setPrivacyLevel(dto.getPrivacyLevel());

        // Upload images if any
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : dto.getImages()) {
                if (!image.isEmpty()) {
                    String imageUrl = cloudinaryService.upload(image);
                    if (imageUrl != null) {
                        imageUrls.add(imageUrl);
                    }
                }
            }
            post.setImageUrls(convertListToJson(imageUrls));
        }

        return postRepository.save(post);
    }

    @Override
    public Post updatePost(Long postId, PostUpdateDto dto, User user) {
        Post post = postRepository.findByIdAndUser(postId, user)
                .orElseThrow(() -> new RuntimeException("Post not found or access denied"));

        post.setContent(dto.getContent());
        post.setPrivacyLevel(dto.getPrivacyLevel());

        // Handle image updates (simplified)
        List<String> newImageUrls = new ArrayList<>();
        if (dto.getExistingImages() != null) {
            newImageUrls.addAll(dto.getExistingImages());
        }

        // Remove deleted images
        if (dto.getImagesToDelete() != null) {
            newImageUrls.removeAll(dto.getImagesToDelete());
        }

        // Add new images
        if (dto.getNewImages() != null && !dto.getNewImages().isEmpty()) {
            for (MultipartFile newImage : dto.getNewImages()) {
                if (!newImage.isEmpty()) {
                    String imageUrl = cloudinaryService.upload(newImage);
                    if (imageUrl != null) {
                        newImageUrls.add(imageUrl);
                    }
                }
            }
        }

        post.setImageUrls(convertListToJson(newImageUrls));
        return postRepository.save(post);
    }

    @Override
    public void deletePost(Long postId, User user) {
        Post post = postRepository.findByIdAndUser(postId, user)
                .orElseThrow(() -> new RuntimeException("Post not found or access denied"));

        post.setDeleted(true);
        postRepository.save(post);
    }

    @Override
    public PostDisplayDto getPostById(Long postId, User currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Simplified - chỉ check owner có thể xem
        if (!canUserViewPost(post, currentUser)) {
            throw new RuntimeException("Access denied");
        }

        return convertToDisplayDto(post, currentUser);
    }

    @Override
    public Post getPostById(long id) {
        return postRepository.findById(id).orElse(null);
    }

    @Override
    public Page<PostDisplayDto> getPostsForNewsFeed(User currentUser, Pageable pageable) {

        Page<Post> posts = postRepository.findVisiblePosts(currentUser.getId(), pageable);

        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }

    @Override
    public Page<PostDisplayDto> getPostsByUser(User targetUser, User currentUser, Pageable pageable) {
        Page<Post> posts;

        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            // Own posts - show all
            posts = postRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(targetUser, pageable);
        } else {
            // Others' posts - show only public
            posts = postRepository.findPublicPostsByUser(targetUser, pageable);
        }

        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }

    @Override
    public Page<PostDisplayDto> getPublicPostsByUser(User targetUser, Pageable pageable) {
        Page<Post> posts = postRepository.findPublicPostsByUser(targetUser, pageable);
        return posts.map(post -> convertToDisplayDto(post, null));
    }

    @Override
    public Page<PostDisplayDto> searchUserPosts(User user, String keyword, Pageable pageable) {
        Page<Post> posts = postRepository.searchPostsByUserAndContent(user, keyword, pageable);
        return posts.map(post -> convertToDisplayDto(post, user));
    }

    @Override
    public boolean toggleLike(Long postId, User user) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!canUserViewPost(post, user)) {
            throw new RuntimeException("Access denied");
        }
        LikePostId likePostId = getLikeStatusId(postId, user.getId());
        boolean isLiked = postLikeRepository.findById(likePostId).isPresent();

        if (isLiked) {
            postLikeRepository.deleteByPostAndUser(post, user);
            likeNotificationService.notifyLikeStatusChanged(postId, getLikeCount(post), false, user.getUsername());
            return false;
        } else {
            LikePost like = new LikePost();
            like.setPost(post);
            like.setUser(user);
            like.setId(likePostId);
            postLikeRepository.save(like);
            likeNotificationService.notifyLikeStatusChanged(postId, getLikeCount(post), true, user.getUsername());
            return true;
        }
    }

    private static LikePostId getLikeStatusId(Long postId, Long userId) {
        LikePostId likeStatusId = new LikePostId();
        likeStatusId.setPostId(postId);
        likeStatusId.setUserId(userId);
        return likeStatusId;
    }

    @Override
    public List<User> getUsersWhoLiked(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return postLikeRepository.findUsersWhoLikedPost(post);
    }

    @Override
    public boolean canUserViewPost(Post post, User viewer) {
        if (post.isDeleted()) {
            return false;
        }

        // Simplified logic - chỉ check owner và public
        if (viewer != null && post.getUser().getId().equals(viewer.getId())) {
            return true; // Owner can view
        }

        return post.getPrivacyLevel() == PrivacyLevel.PUBLIC;
    }

    @Override
    public long countUserPosts(User user) {
        return postRepository.countByUserAndIsDeletedFalse(user);
    }

    // Helper methods
    private PostDisplayDto convertToDisplayDto(Post post, User currentUser) {
        LikePostId likePostId = getLikeStatusId(post.getId(), currentUser.getId());
        boolean isLiked = currentUser != null &&
                postLikeRepository.findById(likePostId).isPresent();

        boolean canEdit = currentUser != null &&
                post.getUser().getId().equals(currentUser.getId());

        boolean canDelete = canEdit;

        PostDisplayDto dto = new PostDisplayDto(post, isLiked, canEdit, canDelete);
        dto.setLikesCount(getLikeCount(post));
        dto.setCommentsCount(countCommentsByPost(post));
        return dto;
    }

    @Override
    public int getLikeCount(Post post) {
        return postLikeRepository.countByPost(post);
    }

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Override
    public int countCommentsByPost(Post post) {
        return postCommentRepository.countByPost(post);
    }

    private String convertListToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }
}