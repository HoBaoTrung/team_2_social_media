package com.codegym.socialmedia.controller.admin;

import com.codegym.socialmedia.service.admin.ManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
public class AdminManagementController {
    @Autowired
    private final ManagementService managementService;

    @GetMapping
    public String getAllLogs(Model model) {
        model.addAttribute("logs", managementService.getAllLogs());
        return "admin/logs"; // Trỏ tới templates/admin/logs.html
    }
}
