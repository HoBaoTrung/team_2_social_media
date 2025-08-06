// File: StatusController.java & UserController.java merged and finalized

package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.*;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.status.StatusService;
import com.codegym.socialmedia.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/status")
public class StatusController {

    @Autowired
    private StatusService statusService;

    @Autowired
    private UserService userService;

    @GetMapping("/news-feed")
    public String showNewsFeed(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size,
                               Model model) {
        try {
            Page<StatusResponseDto> statuses = statusService.getNewsFeed(page, size);

            model.addAttribute("statuses", statuses);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", statuses.getTotalPages());
            model.addAttribute("hasNext", statuses.hasNext());
            model.addAttribute("hasPrevious", statuses.hasPrevious());
            model.addAttribute("statusCreateDto", new StatusCreateDto());

            User currentUser = userService.getCurrentUser();
            if (currentUser != null) {
                model.addAttribute("currentUser", currentUser);
                model.addAttribute("currentUserId", currentUser.getId());
            }

            return "status/news-feed";
        } catch (Exception e) {
            model.addAttribute("error", "Không thể tải news feed: " + e.getMessage());
            return "error/500";
        }
    }

    @GetMapping("/search")
    public String searchStatusPage(@RequestParam(required = false) String q,
                                   @RequestParam(required = false) Long userId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   Model model) {
        if (q != null && !q.trim().isEmpty()) {
            try {
                StatusSearchDto searchDto = new StatusSearchDto();
                searchDto.setQuery(q.trim());
                searchDto.setUserId(userId);
                searchDto.setPage(page);
                searchDto.setSize(size);

                Page<StatusResponseDto> results = statusService.searchStatuses(searchDto);

                model.addAttribute("searchResults", results);
                model.addAttribute("query", q);
                model.addAttribute("userId", userId);
                model.addAttribute("currentPage", page);
                model.addAttribute("totalPages", results.getTotalPages());
                model.addAttribute("hasResults", !results.isEmpty());

                if (userId != null) {
                    User searchUser = userService.getAllUsers().stream()
                            .filter(u -> u.getId().equals(userId))
                            .findFirst().orElse(null);
                    model.addAttribute("searchUser", searchUser);
                }

            } catch (Exception e) {
                model.addAttribute("error", "Lỗi tìm kiếm: " + e.getMessage());
            }
        }

        model.addAttribute("title", "Tìm kiếm bài viết");
        return "status/search-results";
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public String createStatus(@Valid @ModelAttribute StatusCreateDto dto,
                               BindingResult result,
                               @RequestParam(value = "images", required = false) List<MultipartFile> images,
                               @RequestParam(value = "video", required = false) MultipartFile video,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ");
            redirectAttributes.addFlashAttribute("statusCreateDto", dto);
            return "redirect:/status/news-feed";
        }

        try {
            if (images != null && !images.isEmpty()) dto.setImages(images);
            if (video != null && !video.isEmpty()) dto.setVideo(video);

            StatusResponseDto createdStatus = statusService.createStatus(dto);
            redirectAttributes.addFlashAttribute("success", "Đã đăng bài viết thành công!");
            redirectAttributes.addFlashAttribute("newStatusId", createdStatus.getId());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không thể đăng bài: " + e.getMessage());
            redirectAttributes.addFlashAttribute("statusCreateDto", dto);
        }

        return "redirect:/status/news-feed";
    }

    // Các method update, delete, toggle pin, và APIs giữ nguyên như bạn gửi ở trên

    // ------- News Feed fallback (UserController logic) -------
    @GetMapping("/fallback-news-feed")
    public String fallbackNewsFeed(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   Model model) {
        User currentUser = userService.getCurrentUser();

        List<Object> mockStatuses = new ArrayList<>();
        if (currentUser != null) {
            for (int i = 1; i <= size; i++) {
                Map<String, Object> mockStatus = new HashMap<>();
                mockStatus.put("id", (long) (page * size + i));
                mockStatus.put("content", "Bài viết mẫu số " + (page * size + i));
                mockStatus.put("userFullName", currentUser.getFirstName() + " " + currentUser.getLastName());
                mockStatus.put("userAvatar", currentUser.getProfilePicture());
                mockStatus.put("userId", currentUser.getId());
                mockStatus.put("timeAgo", (i * 2) + " giờ trước");
                mockStatus.put("likeCount", (int)(Math.random() * 10));
                mockStatus.put("commentCount", (int)(Math.random() * 5));
                mockStatus.put("shareCount", (int)(Math.random() * 3));
                mockStatus.put("isLiked", Math.random() > 0.5);
                mockStatus.put("isPinned", i == 1 && page == 0);
                mockStatus.put("privacyLevel", "PUBLIC");

                mockStatuses.add(mockStatus);
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Object> mockPage = new PageImpl<>(mockStatuses, pageable, size * 3);

        model.addAttribute("statuses", mockPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", page < 2);
        model.addAttribute("hasPrevious", page > 0);
        model.addAttribute("statusCreateDto", new StatusCreateDto());
        if (currentUser != null) {
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("currentUserId", currentUser.getId());
        }

        return "status/news-feed";
    }
}
