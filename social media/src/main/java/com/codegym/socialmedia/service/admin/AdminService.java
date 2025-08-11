package com.codegym.socialmedia.service.admin;

import com.codegym.socialmedia.model.admin.Admin;
import com.codegym.socialmedia.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    @Qualifier("adminPasswordEncoder")
    private final PasswordEncoder passwordEncoder;

    public List<Admin> getAllAdmins() {
        return adminRepository.findAll();
    }

    public Admin createAdmin(Admin admin) {
        admin.setPasswordHash(passwordEncoder.encode(admin.getPasswordHash()
        ));
        admin.setCreatedAt(LocalDateTime.now());
        return adminRepository.save(admin);
    }

    public Admin updateAdmin(Long id, Admin updated) {
        return adminRepository.findById(id).map(a -> {
            a.setEmail(updated.getEmail());
            a.setFullName(updated.getFullName());
            a.setRole(updated.getRole());
            return adminRepository.save(a);
        }).orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id).orElseThrow(() -> new RuntimeException("Admin not found"));
    }
}
