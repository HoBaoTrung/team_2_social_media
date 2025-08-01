package com.codegym.socialmedia.model.social_action;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.model.admin.Admin;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

//@Entity
//@Table(name = "reports")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
public class Report {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Integer reportId;
//
//    @ManyToOne
//    @JoinColumn(name = "reporter_id", nullable = false)
//    private User reporter;
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "target_type", length = 20, nullable = false)
//    private TargetType targetType;
//
//    private Long targetId; // ID của đối tượng bị báo cáo (user/comment/status)
//
//    @ManyToOne
//    @JoinColumn(name = "report_type_id", nullable = false)
//    private ReportType reportType;
//
//    @Enumerated(EnumType.STRING)
//    @Column(length = 50)
//    private ReportStatus status;
//
//    @Column(columnDefinition = "TEXT")
//    private String reason;
//
//    @Column(columnDefinition = "TEXT")
//    private String adminNote;
//
//    @CreationTimestamp
//    private LocalDateTime createdAt;
//
//    @UpdateTimestamp
//    private LocalDateTime updatedAt;
//
//    @ManyToOne
//    @JoinColumn(name = "resolved_by")
//    private Admin resolvedBy;
//
//    public enum TargetType {
//        USER,
//        COMMENT,
//        STATUS;
//    }
//
//    public enum ReportStatus {
//        PENDING,
//        RESOLVED;
//    }


}
