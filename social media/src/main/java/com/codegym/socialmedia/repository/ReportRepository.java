// ReportRepository.java
package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.admin.Report;
import com.codegym.socialmedia.model.admin.Report.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByStatus(Status status);
}
