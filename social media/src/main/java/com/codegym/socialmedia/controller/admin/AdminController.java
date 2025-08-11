package com.codegym.socialmedia.controller.admin;

import com.codegym.socialmedia.model.admin.Admin;
import com.codegym.socialmedia.service.admin.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    // Trang danh sách admin
    @GetMapping
    public String getAllAdmins(Model model) {
        model.addAttribute("admins", adminService.getAllAdmins());
        return "admin/list"; // templates/admin/list.html
    }

    // Form tạo mới admin
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("admin", new Admin());
        return "admin/create"; // templates/admin/create.html
    }

    @PostMapping("/create")
    public String createAdmin(@ModelAttribute Admin admin) {
        adminService.createAdmin(admin);
        return "redirect:/admin";
    }

    // Form chỉnh sửa admin
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("admin", adminService.getAdminById(id));
        return "admin/edit"; // templates/admin/edit.html
    }

    @PostMapping("/edit/{id}")
    public String updateAdmin(@PathVariable Long id, @ModelAttribute Admin admin) {
        adminService.updateAdmin(id, admin);
        return "redirect:/admin";
    }

    // Xóa admin
    @GetMapping("/delete/{id}")
    public String deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return "redirect:/admin";
    }
    @GetMapping("/admin/login")
    public String adminLogin(Model model) {
        model.addAttribute("isAdmin", true);
        model.addAttribute("loginAction", "/admin/login");
        model.addAttribute("switchLoginUrl", "/login"); // Chuyển sang user login
        return "login"; // dùng chung login.html
    }
}
