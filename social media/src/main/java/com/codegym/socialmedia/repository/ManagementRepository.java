// ManagementRepository.java
package com.codegym.socialmedia.repository;

import com.codegym.socialmedia.model.admin.Managerment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ManagementRepository extends JpaRepository<Managerment, Long> {
}
