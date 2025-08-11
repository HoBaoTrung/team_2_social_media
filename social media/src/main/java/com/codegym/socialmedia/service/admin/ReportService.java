package com.codegym.socialmedia.service.admin;

import com.codegym.socialmedia.model.admin.Managerment;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.admin.Report;
import com.codegym.socialmedia.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static com.codegym.socialmedia.model.admin.Managerment.ActionType.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final IUserRepository userRepository;
    private final ManagementRepository managementRepository;
    private final BlockedRepository blockedUsersRepository;
    private final StatusRepository statusRepository;
    private final CommentRepository commentRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAll();
    }

    public List<Report> getReportsByStatus(Report.Status status) {
        return reportRepository.findByStatus(status);
    }

    @Transactional
    public Report resolveReport(Long id, String adminNote, Managerment.ActionType action, Long adminId) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        Long targetId = report.getTargetId();

        // Thực hiện action
        switch (action) {
            case BLOCK -> blockUser(targetId, adminId);
            case SUSPEND -> suspendUser(targetId);
            case DELETE_STATUS -> deleteStatus(targetId);
            case DELETE_COMMENT -> deleteComment(targetId);
            default -> {}
        }

        // Lưu Moderation Log
        managementRepository.save(
                Managerment.builder()
                        .targetUser(userRepository.findById(targetId).orElse(null))
                        .actionType(action)
                        .actionDate(LocalDateTime.now())
                        .adminNote(adminNote)
                        .build()
        );

        // Cập nhật report
        report.setStatus(Report.Status.RESOLVED);
        report.setAdminNote(adminNote);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(adminId);
        return reportRepository.save(report);
    }

    private void blockUser(Long userId, Long adminId) {
        blockedUsersRepository.save(
                com.codegym.socialmedia.model.social_action.BlockedUsers.builder()
                        .blocker(userRepository.findById(adminId).orElse(null))
                        .blocked(userRepository.findById(userId).orElse(null))
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    private void suspendUser(Long userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setAccountStatus(User.AccountStatus.SUSPENDED);
            userRepository.save(u);
        });
    }

    private void deleteStatus(Long statusId) {
        statusRepository.findById(statusId).ifPresent(s -> {
            s.setDeleted(true);
            statusRepository.save(s);
        });
    }

    private void deleteComment(Long commentId) {
        commentRepository.findById(commentId).ifPresent(c -> {
            c.setDeleted(true);
            commentRepository.save(c);
        });
    }
}
