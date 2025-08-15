package com.codegym.socialmedia.service.post;

import com.codegym.socialmedia.component.CloudinaryService;
import com.codegym.socialmedia.dto.post.PostCreateDto;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.dto.post.PostUpdateDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.model.social_action.PostLike;
import com.codegym.socialmedia.repository.PostLikeRepository;
import com.codegym.socialmedia.repository.PostRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private PostCountService postCountService;

    @Override
    public Post createPost(PostCreateDto dto, User user) {
        // Validate input
        if (!dto.isValid()) {
            throw new IllegalArgumentException(dto.getValidationMessage());
        }

        Post post = new Post();
        post.setUser(user);
        post.setContent(dto.getContent());
        post.setPrivacyLevel(dto.getPrivacyLevel());

        // Set metadata
        post.setLocation(dto.getLocation());
        post.setFeeling(dto.getFeeling());
        post.setActivity(dto.getActivity());
        post.setShareText(dto.getShareText());

        // Handle original post for sharing
        if (dto.getOriginalPostId() != null) {
            Post originalPost = postRepository.findById(dto.getOriginalPostId())
                    .orElseThrow(() -> new RuntimeException("Original post not found"));
            post.setOriginalPost(originalPost);
        }

        // Handle tagged users
        if (dto.getTaggedUsernames() != null && !dto.getTaggedUsernames().isEmpty()) {
            post.setTaggedUsernames(convertListToJson(dto.getTaggedUsernames()));
        }

        // Upload and set images
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

        // Upload and set videos
        if (dto.getVideos() != null && !dto.getVideos().isEmpty()) {
            List<String> videoUrls = new ArrayList<>();
            for (MultipartFile video : dto.getVideos()) {
                if (!video.isEmpty()) {
                    String videoUrl = cloudinaryService.upload(video);
                    if (videoUrl != null) {
                        videoUrls.add(videoUrl);
                    }
                }
            }
            post.setVideoUrls(convertListToJson(videoUrls));
        }

        // Determine and set post type
        post.determineAndSetPostType();

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

        // Re-determine post type after update
        post.determineAndSetPostType();

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

        if (!canUserViewPost(post, currentUser)) {
            throw new RuntimeException("Access denied");
        }

        return convertToDisplayDto(post, currentUser);
    }

    @Override
    public Page<PostDisplayDto> getPostsForNewsFeed(User currentUser, Pageable pageable) {
        // TẠM THỜI simplified - chỉ lấy posts của chính user
        Page<Post> posts = postRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(currentUser, pageable);
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

        boolean isLiked = postLikeRepository.existsByPostAndUser(post, user);

        if (isLiked) {
            postLikeRepository.deleteByPostAndUser(post, user);
            return false;
        } else {
            PostLike like = new PostLike(post, user);
            postLikeRepository.save(like);
            return true;
        }
    }

    @Override
    public List<User> getUsersWhoLiked(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return postLikeRepository.findUsersWhoLikedPost(post);
    }

    @Override
    public boolean toggleReaction(Long postId, User user, String reactionType) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (!canUserViewPost(post, user)) {
            throw new RuntimeException("Access denied");
        }

        // For now, treat all reactions as likes (can be extended later)
        return toggleLike(postId, user);
    }

    @Override
    public boolean savePost(Long postId, User user) {
        // Implementation for saving posts (bookmark feature)
        // This would require a SavedPost entity and repository
        // For now, return true as placeholder
        return true;
    }

    @Override
    public boolean unsavePost(Long postId, User user) {
        // Implementation for unsaving posts
        // For now, return true as placeholder
        return true;
    }

    @Override
    public Page<PostDisplayDto> getSavedPosts(User user, Pageable pageable) {
        // Implementation for getting saved posts
        // For now, return empty page
        return Page.empty(pageable);
    }

    @Override
    public void recordView(Long postId, User user) {
        // Implementation for recording post views (analytics)
        // This could be async and stored in a separate table
        // For now, just log
        System.out.println("User " + user.getId() + " viewed post " + postId);
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

        return post.getPrivacyLevel() == Post.PrivacyLevel.PUBLIC;
    }

    @Override
    public long countUserPosts(User user) {
        return postRepository.countByUserAndIsDeletedFalse(user);
    }

    @Override
    public Page<PostDisplayDto> getMediaPostsByUser(User targetUser, User currentUser, Pageable pageable) {
        Page<Post> posts;

        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            // Own posts - show all media posts
            posts = postRepository.findByUserAndPostTypeInAndIsDeletedFalseOrderByCreatedAtDesc(
                    targetUser,
                    List.of(Post.PostType.IMAGE, Post.PostType.VIDEO),
                    pageable
            );
        } else {
            // Others' posts - show only public media posts
            posts = postRepository.findByUserAndPostTypeInAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
                    targetUser,
                    List.of(Post.PostType.IMAGE, Post.PostType.VIDEO),
                    Post.PrivacyLevel.PUBLIC,
                    pageable
            );
        }

        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }

    @Override
    public List<Map<String, String>> getUserPhotos(User targetUser, User currentUser) {
        List<Post> posts;

        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            // Own posts - get all image posts
            posts = postRepository.findByUserAndPostTypeAndIsDeletedFalseOrderByCreatedAtDesc(
                    targetUser, Post.PostType.IMAGE
            );
        } else {
            // Others' posts - get only public image posts
            posts = postRepository.findByUserAndPostTypeAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
                    targetUser, Post.PostType.IMAGE, Post.PrivacyLevel.PUBLIC
            );
        }

        List<Map<String, String>> photos = new ArrayList<>();
        for (Post post : posts) {
            List<String> imageUrls = parseJsonToList(post.getImageUrls());
            for (String imageUrl : imageUrls) {
                Map<String, String> photo = new HashMap<>();
                photo.put("url", imageUrl);
                photo.put("postId", post.getId().toString());
                photo.put("createdAt", post.getCreatedAt().toString());
                photos.add(photo);
            }
        }

        return photos;
    }

    @Override
    public List<Map<String, String>> getUserVideos(User targetUser, User currentUser) {
        List<Post> posts;

        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            // Own posts - get all video posts
            posts = postRepository.findByUserAndPostTypeAndIsDeletedFalseOrderByCreatedAtDesc(
                    targetUser, Post.PostType.VIDEO
            );
        } else {
            // Others' posts - get only public video posts
            posts = postRepository.findByUserAndPostTypeAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
                    targetUser, Post.PostType.VIDEO, Post.PrivacyLevel.PUBLIC
            );
        }

        List<Map<String, String>> videos = new ArrayList<>();
        for (Post post : posts) {
            List<String> videoUrls = parseJsonToList(post.getVideoUrls());
            for (String videoUrl : videoUrls) {
                Map<String, String> video = new HashMap<>();
                video.put("url", videoUrl);
                video.put("postId", post.getId().toString());
                video.put("createdAt", post.getCreatedAt().toString());
                videos.add(video);
            }
        }

        return videos;
    }

    @Override
    public Post sharePost(Long originalPostId, String shareText, User user, Post.PrivacyLevel privacyLevel) {
        Post originalPost = postRepository.findById(originalPostId)
                .orElseThrow(() -> new RuntimeException("Original post not found"));

        if (!canUserViewPost(originalPost, user)) {
            throw new RuntimeException("You cannot share this post");
        }

        Post sharePost = new Post();
        sharePost.setUser(user);
        sharePost.setContent(shareText != null ? shareText : "");
        sharePost.setOriginalPost(originalPost);
        sharePost.setShareText(shareText);
        sharePost.setPrivacyLevel(privacyLevel);
        sharePost.setPostType(Post.PostType.SHARED);

        return postRepository.save(sharePost);
    }

    @Override
    public Page<PostDisplayDto> getSharedPosts(Long originalPostId, Pageable pageable) {
        Post originalPost = postRepository.findById(originalPostId)
                .orElseThrow(() -> new RuntimeException("Original post not found"));

        Page<Post> sharedPosts = postRepository.findByOriginalPostAndIsDeletedFalseOrderByCreatedAtDesc(
                originalPost, pageable
        );

        return sharedPosts.map(post -> convertToDisplayDto(post, null));
    }

    @Override
    public boolean canUserEditPost(Post post, User user) {
        return user != null && post.getUser().getId().equals(user.getId());
    }

    @Override
    public boolean canUserDeletePost(Post post, User user) {
        return canUserEditPost(post, user);
    }

    @Override
    public long countUserMediaPosts(User user) {
        return postRepository.countByUserAndPostTypeInAndIsDeletedFalse(
                user, List.of(Post.PostType.IMAGE, Post.PostType.VIDEO)
        );
    }

    @Override
    public Map<String, Long> getPostStatistics(User user) {
        Map<String, Long> stats = new HashMap<>();

        stats.put("totalPosts", countUserPosts(user));
        stats.put("mediaPosts", countUserMediaPosts(user));
        stats.put("textPosts", postRepository.countByUserAndPostTypeAndIsDeletedFalse(user, Post.PostType.TEXT));
        stats.put("sharedPosts", postRepository.countByUserAndPostTypeAndIsDeletedFalse(user, Post.PostType.SHARED));

        // Tính tổng likes và comments
        List<Post> userPosts = postRepository.findByUserAndIsDeletedFalseOrderByCreatedAtDesc(user);
        long totalLikes = userPosts.stream().mapToLong(Post::getLikesCount).sum();
        long totalComments = userPosts.stream().mapToLong(Post::getCommentsCount).sum();

        stats.put("totalLikes", totalLikes);
        stats.put("totalComments", totalComments);

        return stats;
    }

    @Override
    public Page<PostDisplayDto> getPostsByType(User targetUser, User currentUser, Post.PostType postType, Pageable pageable) {
        Page<Post> posts;

        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            // Own posts - show all posts of specified type
            posts = postRepository.findByUserAndPostTypeAndIsDeletedFalseOrderByCreatedAtDesc(
                    targetUser, postType, pageable
            );
        } else {
            // Others' posts - show only public posts of specified type
            posts = postRepository.findByUserAndPostTypeAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
                    targetUser, postType, Post.PrivacyLevel.PUBLIC, pageable
            );
        }

        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }

    @Override
    public Page<PostDisplayDto> getPostsByLocation(String location, User currentUser, Pageable pageable) {
        Page<Post> posts = postRepository.findByLocationContainingIgnoreCaseAndPrivacyLevelAndIsDeletedFalseOrderByCreatedAtDesc(
                location, Post.PrivacyLevel.PUBLIC, pageable
        );

        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }

    @Override
    public Page<PostDisplayDto> getPostsWithTag(String username, User currentUser, Pageable pageable) {
        // Tìm posts có tag username trong taggedUsernames JSON
        Page<Post> posts = postRepository.findPostsWithTaggedUser(username, pageable);

        return posts.map(post -> convertToDisplayDto(post, currentUser));
    }

    // Helper methods
    private PostDisplayDto convertToDisplayDto(Post post, User currentUser) {
        boolean isLiked = currentUser != null &&
                postLikeRepository.existsByPostAndUser(post, currentUser);

        boolean canEdit = currentUser != null &&
                post.getUser().getId().equals(currentUser.getId());

        boolean canDelete = canEdit;

        return new PostDisplayDto(post, isLiked, canEdit, canDelete);
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

    private List<String> parseJsonToList(String json) {
        if (json == null || json.trim().isEmpty() || "[]".equals(json)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Import statements cần thiết
    private Map<String, Object> createPhotoMap(String url, String postId, String createdAt) {
        Map<String, Object> photo = new HashMap<>();
        photo.put("url", url);
        photo.put("postId", postId);
        photo.put("createdAt", createdAt);
        return photo;
    }
}