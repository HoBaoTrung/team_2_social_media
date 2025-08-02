package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.UserPasswordDto;
import com.codegym.socialmedia.dto.UserUpdateDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/setting")
    public String showProfile(Model model) {
        // Lấy thông tin người dùng hiện tại (ưu tiên sử dụng Spring Security nếu có)
        User user;
        try {
            // Sử dụng getCurrentUser() nếu tích hợp Spring Security
            user = userService.getCurrentUser();
            if (user == null) {
                // Fallback: Lấy user mặc định nếu không có người dùng hiện tại
                user = userService.getUserByUsername("john_doe");
            }
        } catch (Exception e) {
            // Xử lý lỗi (ví dụ: user không tồn tại)
            model.addAttribute("error", "Unable to load user profile.");
            user = new User(); // Tạo user rỗng để tránh lỗi null
        }

        UserUpdateDto userUpdateDto = new UserUpdateDto(user);
        // Thêm các attribute cho template
        model.addAttribute("title", "User Profile");
        model.addAttribute("user", userUpdateDto);
        model.addAttribute("passwordDto", new UserPasswordDto());
        // Trả về layout chung
        return "profile/index";
    }

    @PostMapping("/setting")
    public String updateProfile(@Valid @ModelAttribute("user") UserUpdateDto dto, BindingResult result,
                                @RequestParam("avatarFile") MultipartFile avatarFile
    ) {
        if (result.hasErrors()) {
            // Đưa lỗi và product vào flash attribute
            return "profile/index";
        }
        User user = userService.getUserByUsername("john_doe");

        userService.save(dto.toUser(user), avatarFile);
        return "redirect:/setting";
    }

    @PostMapping("/setting/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") UserPasswordDto dto,
                                 BindingResult result, Model model) {

        String currentPassword = dto.getCurrentPassword();
        String newPassword = dto.getNewPassword();
        String confirmPassword = dto.getConfirmPassword();

        User user = userService.getUserByUsername("john_doe");

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            result.rejectValue("currentPassword", "error.currentPassword", "Mật khẩu hiện tại không đúng");
        }

        if (newPassword.equals(currentPassword)) {
            result.rejectValue("newPassword", "error.newPassword", "Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }

        if (!newPassword.equals(confirmPassword)) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
        }

        if (result.hasErrors()) {
            // Trả về đúng tab change-password đang mở
            model.addAttribute("passwordDto", dto);
            model.addAttribute("user", new UserUpdateDto(user));
            model.addAttribute("title", "User Profile");
            model.addAttribute("activeTab", "password"); // Dùng để mở đúng tab
            return "profile/index";
        }
        user.setPasswordHash(newPassword);

        userService.save(user);

        return "redirect:/setting";
    }

}
