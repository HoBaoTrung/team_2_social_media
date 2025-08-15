package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.component.PrivacyUtils;
import com.codegym.socialmedia.dto.UserDTO;
import com.codegym.socialmedia.dto.UserRegistrationDto;
import com.codegym.socialmedia.dto.UserPasswordDto;
import com.codegym.socialmedia.dto.UserRegistrationDto;
import com.codegym.socialmedia.dto.UserUpdateDto;
import com.codegym.socialmedia.dto.friend.FriendDto;
import com.codegym.socialmedia.general_interface.NormalRegister;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.account.UserPrivacySettings;
import com.codegym.socialmedia.model.social_action.Friendship;
import com.codegym.socialmedia.model.social_action.Status;
import com.codegym.socialmedia.repository.UserPrivacySettingsRepository;
import com.codegym.socialmedia.service.friend_ship.FriendshipService;
import com.codegym.socialmedia.service.user.UserService;
import com.codegym.socialmedia.service.user.UserStatsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserPrivacySettingsRepository privacySettingsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @Autowired
    private FriendshipService friendshipService;

    @Autowired
    private UserStatsService userStatsService;
    // =============== PROFILE ===============

    @GetMapping("/profile/{username}")
    public String viewProfile(
            @RequestParam(value = "filter", defaultValue = "posts") String filter,
            @PathVariable String username,
            Model model) {

        User viewedUser = userService.getUserByUsername(username);
        User currentUser = userService.getCurrentUser();

        boolean isOwner = currentUser != null
                && viewedUser != null
                && currentUser.getId() != null
                && currentUser.getId().equals(viewedUser.getId());

        // Friendship check
        Friendship friendship = null;
        if (!isOwner && currentUser != null) {
            friendship = friendshipService.findByUsers(currentUser.getId(), viewedUser.getId());
            if (friendship != null) {
                boolean isSender = friendship.getRequester() != null
                        && currentUser.getId().equals(friendship.getRequester().getId());
                boolean isReceiver = friendship.getAddressee() != null
                        && currentUser.getId().equals(friendship.getAddressee().getId());
                model.addAttribute("isSender", isSender);
                model.addAttribute("isReceiver", isReceiver);
            }
        }

        Friendship.FriendshipStatus friendshipStatus =
                friendshipService.getFriendshipStatus(viewedUser, currentUser);
        boolean isFriend = (friendshipStatus == Friendship.FriendshipStatus.ACCEPTED);

        UserPrivacySettings privacy = viewedUser.getPrivacySettings();

        // Các quyền hiển thị
        model.addAttribute("canViewEmail", PrivacyUtils.canView(currentUser, viewedUser, privacy.getShowEmail(), isFriend));
        model.addAttribute("canViewPhone", PrivacyUtils.canView(currentUser, viewedUser, privacy.getShowPhone(), isFriend));
        model.addAttribute("canViewDob", PrivacyUtils.canView(currentUser, viewedUser, privacy.getShowDob(), isFriend));
        model.addAttribute("canViewBio", PrivacyUtils.canView(currentUser, viewedUser, privacy.getShowBio(), isFriend));
        model.addAttribute("canSendMessage", PrivacyUtils.canView(currentUser, viewedUser, privacy.getAllowSendMessage(), isFriend));
        model.addAttribute("canViewFriendList", PrivacyUtils.canView(currentUser, viewedUser, privacy.getShowFriendList(), isFriend));
        model.addAttribute("allowFriendRequests", privacy.isAllowFriendRequests());

        // Danh sách bạn cho tab Friends/Mutual
        Page<FriendDto> friends = Page.empty();
        if ("mutual".equalsIgnoreCase(filter)
                || (!isFriend && !privacy.getShowFriendList().equals(UserPrivacySettings.PrivacyLevel.PUBLIC))) {
            friends = friendshipService.findMutualFriends(viewedUser.getId(), currentUser.getId(), 0, 10);
        } else if (isOwner || isFriend || !privacy.getShowFriendList().equals(UserPrivacySettings.PrivacyLevel.PRIVATE)) {
            friends = friendshipService.getVisibleFriendList(viewedUser, 0, 10);
        } else {
            friends = Page.empty(); // Tránh NullPointer
        }

        model.addAttribute("friends", friends.getContent());
        model.addAttribute("friendCount", friendshipService.countFriends(viewedUser.getId()));
        model.addAttribute("mutualFriendsCount", friendshipService.countMutualFriends(
                currentUser != null ? currentUser.getId() : null,
                viewedUser.getId()
        ));
        model.addAttribute("user", viewedUser);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("friendshipStatus", friendshipStatus.name());
        model.addAttribute("targetUserId", viewedUser.getId());
        model.addAttribute("filter", filter);

        // Placeholder cho posts (sau bạn bind từ PostService)
        model.addAttribute("posts", new ArrayList<>());

        return "profile/view";
    }

    // =============== AUTH / NAV ===============

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping({"/login", "/admin/login"})
    public String loginForm(
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model, HttpServletRequest request) {
        boolean isAdmin = request.getRequestURI().startsWith("/admin");
        model.addAttribute("loginAction", isAdmin ? "/admin/login" : "/login");
        model.addAttribute("switchLoginUrl", isAdmin ? "/login" : "/admin/login");
        model.addAttribute("isAdmin", isAdmin);
        // Luôn thêm object user để tránh lỗi template
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }

        if (logout != null) {
            model.addAttribute("message", "Đăng xuất thành công!");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Validated(NormalRegister.class)
                               @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Kiểm tra password confirm
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Mật khẩu xác nhận không khớp");
        }

        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            // Đảm bảo object user vẫn có trong model khi có lỗi
            model.addAttribute("user", registrationDto);
            model.addAttribute("isAdmin", false);
            return "login"; // Trả về trang login với form đăng ký active
        }


        try {
            userService.save(registrationDto);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            redirectAttributes.addFlashAttribute("username", registrationDto.getUsername());
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại.");
            model.addAttribute("user", registrationDto);
            model.addAttribute("isAdmin", false);
            return "login";
        }
    }


    @GetMapping("/oauth2/login-success")
    public String oauth2LoginSuccess() {
        return "redirect:/news-feed";
    }

    // =============== SETTINGS ===============

    @GetMapping("/setting")
    public String showProfile(Model model) {
        User user;
        try {
            user = userService.getCurrentUser();
            if (user == null) {
                user = userService.getUserByUsername("john_doe");
            }
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load user profile.");
            user = new User();
        }

        // Nếu không có flash attribute thì tạo mới
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserUpdateDto(user));
        }

        // Nếu không có flash cho đổi mật khẩu
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new UserPasswordDto());
        }

        UserPrivacySettings settings = user.getPrivacySettings();
        if (!model.containsAttribute("privacySettings")) {
            model.addAttribute("privacySettings", settings);
        }
        model.addAttribute("privacyLevels", UserPrivacySettings.PrivacyLevel.values());
        model.addAttribute("title", "User Profile");
        return "profile/index";
    }


    @PostMapping("/setting")
    public String updateProfile(@Valid @ModelAttribute("user") UserUpdateDto dto,
                                BindingResult result,
                                @RequestParam("avatarFile") MultipartFile avatarFile,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            // Đưa DTO và lỗi vào flash để dùng sau redirect
            redirectAttributes.addFlashAttribute("user", dto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", result);
            return "redirect:/setting";
        }

        User user = userService.getCurrentUser();
        User updatedUser = userService.save(dto.toUser(user), avatarFile);

        userService.refreshAuthentication(updatedUser.getUsername());

        redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công");
        return "redirect:/setting";
    }

    @PostMapping("/setting/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") UserPasswordDto dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser();
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            result.rejectValue("currentPassword", "error.currentPassword", "Mật khẩu hiện tại không đúng");
        }

        if (dto.getNewPassword().equals(dto.getCurrentPassword())) {
            result.rejectValue("newPassword", "error.newPassword", "Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
        }

        if (result.hasErrors()) {
            // Truyền lại lỗi và dữ liệu qua flash
            redirectAttributes.addFlashAttribute("passwordDto", dto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.passwordDto", result);
            // Đảm bảo tab active là password
            redirectAttributes.addFlashAttribute("activeTab", "password");
            return "redirect:/setting";
        }

        try {
            user.setPasswordHash(dto.getNewPassword());
            userService.save(user);
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi đổi mật khẩu");
        }

        redirectAttributes.addFlashAttribute("activeTab", "password");
        return "redirect:/setting";
    }

    @PostMapping("/setting/privacy")
    public String updatePrivacySettings(@ModelAttribute UserPrivacySettings dto, RedirectAttributes redirect) {
        privacySettingsRepository.save(dto);
        redirect.addFlashAttribute("success", "Cập nhật quyền riêng tư thành công.");
        return "redirect:/setting";
    }

    @GetMapping("/api/user/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getCurrentUserStats() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Map<String, Long> stats = userStatsService.getUserStats(currentUser);
        return ResponseEntity.ok(stats);
    }
}