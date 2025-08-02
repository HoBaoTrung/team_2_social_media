package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.UserUpdateDto;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
        return "redirect:/profile";
    }
}
