package com.codegym.socialmedia.service.admin;

import com.codegym.socialmedia.model.admin.Managerment;
import com.codegym.socialmedia.repository.ManagementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagementService {

    private final ManagementRepository managementRepository;

    public List<Managerment> getAllLogs() {
        return managementRepository.findAll();
    }
}
