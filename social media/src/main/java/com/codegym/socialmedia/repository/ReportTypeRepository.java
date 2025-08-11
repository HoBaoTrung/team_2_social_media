// ReportTypeRepository.java
package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.admin.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportTypeRepository extends JpaRepository<ReportType, Long> {
}
