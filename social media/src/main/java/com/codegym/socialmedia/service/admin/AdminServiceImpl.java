package com.codegym.socialmedia.service.admin;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.repository.IUserRepository;
import com.codegym.socialmedia.repository.TrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private TrackingRepository trackingRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void blockUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setActive(false); // Giả sử có field 'active'
            userRepository.save(user);
        });
    }

    @Override
    public Map<String, Long> getVisitStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("today", trackingRepository.countVisitsOn(LocalDate.now()));
        stats.put("week", trackingRepository.countVisitsFrom(LocalDate.now().minusDays(7)));
        stats.put("month", trackingRepository.countVisitsFrom(LocalDate.now().minusDays(30)));
        return stats;
    }

    @Override
    public Map<String, Long> getNewUserStatistics() {
        Map<String, Long> stats = new HashMap<>();

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(23, 59, 59);

        stats.put("today", userRepository.countByCreatedAtBetween(startOfToday, endOfToday));
        stats.put("week", userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(7)));
        stats.put("month", userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30)));

        return stats;
    }

}
