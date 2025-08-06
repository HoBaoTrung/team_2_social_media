package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    // Hiển thị dashboard với tất cả thông tin
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<User> users = adminService.getAllUsers();
        Map<String, Long> visitStats = adminService.getVisitStatistics();
        Map<String, Long> newUserStats = adminService.getNewUserStatistics();

        model.addAttribute("users", users);
        model.addAttribute("visitStats", visitStats);
        model.addAttribute("newUserStats", newUserStats);

        return "admin/dashboard"; // Tên file Thymeleaf: dashboard.html
    }

    // Block user (gọi bằng nút trong Thymeleaf form hoặc JS)
    @PostMapping("/block/{userId}")
    public String blockUser(@PathVariable Long userId) {
        adminService.blockUser(userId);
        return "redirect:/admin/dashboard"; // Sau khi block xong, reload lại dashboard
    }
}
