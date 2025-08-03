package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.UserRegistrationDto;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class UserController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {

        // Luôn thêm object user để tránh lỗi template
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }

        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không đúng!");
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
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        // Kiểm tra lỗi validation
        if (result.hasErrors()) {
            // Đảm bảo object user vẫn có trong model khi có lỗi
            model.addAttribute("user", registrationDto);
            return "login"; // Trả về trang login với form đăng ký active
        }

        // Kiểm tra password confirm
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", null, "Mật khẩu xác nhận không khớp");
            model.addAttribute("user", registrationDto);
            return "login";
        }

        try {
            // Kiểm tra username và email đã tồn tại
            if (userService.existsByUsername(registrationDto.getUsername())) {
                result.rejectValue("username", null, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác");
                model.addAttribute("user", registrationDto);
                return "login";
            }

            if (userService.existsByEmail(registrationDto.getEmail())) {
                result.rejectValue("email", null, "Email đã được sử dụng, vui lòng chọn email khác");
                model.addAttribute("user", registrationDto);
                return "login";
            }

            // Đăng ký người dùng

            User newUser = new User();

            userService.save(registrationDto);

            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Vui lòng đăng nhập.");
            redirectAttributes.addFlashAttribute("username", registrationDto.getUsername());

            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại.");
            model.addAttribute("user", registrationDto);
            return "login";
        }
    }

    @GetMapping("/news-feed")
    public String newsFeed(Model model) {
        // Trang news feed sau khi đăng nhập thành công
        return "news-feed";
    }

    @GetMapping("/oauth2/login-success")
    public String oauth2LoginSuccess() {
        return "redirect:/news-feed";
    }

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
        User user = userService.getCurrentUser();

        userService.save(dto.toUser(user), avatarFile);
        return "redirect:/setting";
    }

    @PostMapping("/setting/change-password")
    public String changePassword(@Valid @ModelAttribute("passwordDto") UserPasswordDto dto,
                                 BindingResult result, Model model) {

        String currentPassword = dto.getCurrentPassword();
        String newPassword = dto.getNewPassword();
        String confirmPassword = dto.getConfirmPassword();

        User user = userService.getCurrentUser();

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