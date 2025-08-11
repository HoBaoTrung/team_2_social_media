package com.codegym.socialmedia.controller.admin;

import com.codegym.socialmedia.model.admin.Managerment;
import com.codegym.socialmedia.model.admin.Report;
import com.codegym.socialmedia.service.admin.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final ReportService reportService;

    // Hiển thị danh sách báo cáo
    @GetMapping
    public String getReports(@RequestParam(required = false) Report.Status status, Model model) {
        List<Report> reports = (status == null)
                ? reportService.getAllReports()
                : reportService.getReportsByStatus(status);
        model.addAttribute("reports", reports);
        return "admin/reports"; // templates/admin/reports.html
    }

    // Xử lý báo cáo
    @PostMapping("/{id}/resolve")
    public String resolveReport(@PathVariable Long id,
                                @RequestParam String note,
                                @RequestParam Managerment.ActionType action,
                                @RequestParam Long adminId) {
        reportService.resolveReport(id, note, action, adminId);
        return "redirect:/admin/reports";
    }
}
