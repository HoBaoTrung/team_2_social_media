package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.post.PostCreateDto;
import com.codegym.socialmedia.dto.post.PostDisplayDto;
import com.codegym.socialmedia.dto.post.PostUpdateDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.social_action.Post;
import com.codegym.socialmedia.service.post.PostService;
import com.codegym.socialmedia.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    // ================== WEB PAGES ==================

    @GetMapping
    public String postsPage(Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostDisplayDto> posts = postService.getPostsForNewsFeed(currentUser, pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("postCreateDto", new PostCreateDto());
        model.addAttribute("privacyLevels", Post.PrivacyLevel.values());

        return "posts/index";
    }

    @GetMapping("/user/{username}")
    public String userPosts(@PathVariable String username,
                            Model model,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size) {
        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            model.addAttribute("error", "Người dùng không tồn tại");
            return "error/404";
        }

        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<PostDisplayDto> posts;
        if (currentUser != null) {
            posts = postService.getPostsByUser(targetUser, currentUser, pageable);
        } else {
            posts = postService.getPublicPostsByUser(targetUser, pageable);
        }

        model.addAttribute("posts", posts);
        model.addAttribute("targetUser", targetUser);
        model.addAttribute("isOwner", currentUser != null && currentUser.getId().equals(targetUser.getId()));

        if (currentUser != null && currentUser.getId().equals(targetUser.getId())) {
            model.addAttribute("postCreateDto", new PostCreateDto());
            model.addAttribute("privacyLevels", Post.PrivacyLevel.values());
        }

        return "posts/user-posts";
    }

    @GetMapping("/search")
    public String searchPosts(@RequestParam String keyword,
                              Model model,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostDisplayDto> posts = postService.searchUserPosts(currentUser, keyword, pageable);

        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);

        return "posts/search-results";
    }

    // ================== CRUD OPERATIONS ==================

    @PostMapping("/create")
    public String createPost(@Valid @ModelAttribute PostCreateDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
            return "redirect:/news-feed";
        }

        try {
            postService.createPost(dto, currentUser);
            redirectAttributes.addFlashAttribute("success", "Đăng bài thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/news-feed";
    }

    @PostMapping("/update/{id}")
    public String updatePost(@PathVariable Long id,
                             @Valid @ModelAttribute PostUpdateDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng kiểm tra lại thông tin");
            return "redirect:/posts";
        }

        try {
            dto.setId(id);
            postService.updatePost(id, dto, currentUser);
            redirectAttributes.addFlashAttribute("success", "Cập nhật bài viết thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/posts";
    }

    @PostMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            postService.deletePost(id, currentUser);
            redirectAttributes.addFlashAttribute("success", "Xóa bài viết thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/posts";
    }

    // ================== AJAX API ENDPOINTS ==================

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPostApi(@Valid @ModelAttribute PostCreateDto dto,
                                                             BindingResult result) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Post post = postService.createPost(dto, currentUser);
            PostDisplayDto postDto = postService.getPostById(post.getId(), currentUser);

            response.put("success", true);
            response.put("message", "Đăng bài thành công!");
            response.put("post", postDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/feed")
    @ResponseBody
    public ResponseEntity<Page<PostDisplayDto>> getNewsFeed(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostDisplayDto> posts = postService.getPostsForNewsFeed(currentUser, pageable);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/api/user/{username}")
    @ResponseBody
    public ResponseEntity<Page<PostDisplayDto>> getUserPosts(
            @PathVariable String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<PostDisplayDto> posts;
        if (currentUser != null) {
            posts = postService.getPostsByUser(targetUser, currentUser, pageable);
        } else {
            posts = postService.getPublicPostsByUser(targetUser, pageable);
        }

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/api/user/{username}/photos")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getUserPhotos(@PathVariable String username) {
        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = userService.getCurrentUser();

        try {
            List<Map<String, String>> photos = postService.getUserPhotos(targetUser, currentUser);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/api/like/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean isLiked = postService.toggleLike(id, currentUser);

            response.put("success", true);
            response.put("isLiked", isLiked);
            response.put("message", isLiked ? "Đã thích" : "Đã bỏ thích");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<PostDisplayDto> getPost(@PathVariable Long id) {
        User currentUser = userService.getCurrentUser();

        try {
            PostDisplayDto post = postService.getPostById(id, currentUser);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updatePostApi(@PathVariable Long id,
                                                             @Valid @ModelAttribute PostUpdateDto dto,
                                                             BindingResult result) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("message", "Dữ liệu không hợp lệ");
            response.put("errors", result.getAllErrors());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            dto.setId(id);
            Post post = postService.updatePost(id, dto, currentUser);
            PostDisplayDto postDto = postService.getPostById(post.getId(), currentUser);

            response.put("success", true);
            response.put("message", "Cập nhật bài viết thành công!");
            response.put("post", postDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deletePostApi(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            postService.deletePost(id, currentUser);
            response.put("success", true);
            response.put("message", "Xóa bài viết thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ================== MEDIA AND ADVANCED ENDPOINTS ==================

    @GetMapping("/api/user/{username}/media")
    @ResponseBody
    public ResponseEntity<Page<PostDisplayDto>> getUserMediaPosts(
            @PathVariable String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = userService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        Page<PostDisplayDto> posts = postService.getMediaPostsByUser(targetUser, currentUser, pageable);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/api/user/{username}/videos")
    @ResponseBody
    public ResponseEntity<List<Map<String, String>>> getUserVideos(@PathVariable String username) {
        User targetUser = userService.getUserByUsername(username);
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        User currentUser = userService.getCurrentUser();

        try {
            List<Map<String, String>> videos = postService.getUserVideos(targetUser, currentUser);
            return ResponseEntity.ok(videos);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/api/share/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> sharePost(
            @PathVariable Long id,
            @RequestParam(required = false) String shareText,
            @RequestParam(defaultValue = "PUBLIC") String privacyLevel) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            Post.PrivacyLevel privacy = Post.PrivacyLevel.valueOf(privacyLevel.toUpperCase());
            Post sharedPost = postService.sharePost(id, shareText, currentUser, privacy);
            PostDisplayDto postDto = postService.getPostById(sharedPost.getId(), currentUser);

            response.put("success", true);
            response.put("message", "Chia sẻ bài viết thành công!");
            response.put("post", postDto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/posts/shared/{id}")
    @ResponseBody
    public ResponseEntity<Page<PostDisplayDto>> getSharedPosts(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<PostDisplayDto> sharedPosts = postService.getSharedPosts(id, pageable);
            return ResponseEntity.ok(sharedPosts);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/api/posts/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getCurrentUserPostStatistics() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Long> stats = postService.getPostStatistics(currentUser);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/posts/type/{postType}")
    @ResponseBody
    public ResponseEntity<Page<PostDisplayDto>> getPostsByType(
            @PathVariable String postType,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        User targetUser = username != null ? userService.getUserByUsername(username) : currentUser;
        if (targetUser == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Post.PostType type = Post.PostType.valueOf(postType.toUpperCase());
            Pageable pageable = PageRequest.of(page, size);
            Page<PostDisplayDto> posts = postService.getPostsByType(targetUser, currentUser, type, pageable);
            return ResponseEntity.ok(posts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ================== REACTION ENDPOINTS ==================

    @PostMapping("/api/{id}/reaction")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleReaction(
            @PathVariable Long id,
            @RequestParam(defaultValue = "LIKE") String reactionType) {

        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean hasReaction = postService.toggleReaction(id, currentUser, reactionType);

            response.put("success", true);
            response.put("hasReaction", hasReaction);
            response.put("reactionType", reactionType);
            response.put("message", hasReaction ? "Đã thêm reaction" : "Đã bỏ reaction");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ================== SAVE/BOOKMARK ENDPOINTS ==================

    @PostMapping("/api/{id}/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> savePost(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            boolean saved = postService.savePost(id, currentUser);

            response.put("success", true);
            response.put("saved", saved);
            response.put("message", saved ? "Đã lưu bài viết" : "Đã bỏ lưu bài viết");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/saved")
    @ResponseBody
    public ResponseEntity<Page<PostDisplayDto>> getSavedPosts(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<PostDisplayDto> savedPosts = postService.getSavedPosts(currentUser, pageable);
        return ResponseEntity.ok(savedPosts);
    }

    // ================== VIEW TRACKING ==================

    @PostMapping("/api/{id}/view")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordView(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        User currentUser = userService.getCurrentUser();

        if (currentUser == null) {
            response.put("success", false);
            response.put("message", "Vui lòng đăng nhập");
            return ResponseEntity.status(401).body(response);
        }

        try {
            postService.recordView(id, currentUser);

            response.put("success", true);
            response.put("message", "View recorded");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}