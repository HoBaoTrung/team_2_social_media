package com.codegym.socialmedia.service.admin;

import com.codegym.socialmedia.model.account.User;

import java.util.List;
import java.util.Map;

public interface AdminService {
    List<User> getAllUsers();
    void blockUser(Long userId);
    Map<String, Long> getVisitStatistics();
    Map<String, Long> getNewUserStatistics();
}


