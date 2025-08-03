package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public String getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            StringBuilder result = new StringBuilder();
            result.append("Total users: ").append(users.size()).append("\n\n");

            for (User user : users) {
                result.append("ID: ").append(user.getId()).append("\n");
                result.append("Username: ").append(user.getUsername()).append("\n");
                result.append("Email: ").append(user.getEmail()).append("\n");
                result.append("First Name: ").append(user.getFirstName()).append("\n");
                result.append("Last Name: ").append(user.getLastName()).append("\n");
                result.append("Login Method: ").append(user.getLoginMethod()).append("\n");
                result.append("Account Status: ").append(user.getAccountStatus()).append("\n");
                result.append("Is Active: ").append(user.isActive()).append("\n");
                result.append("Is Verified: ").append(user.isVerified()).append("\n");
                result.append("Created At: ").append(user.getCreatedAt()).append("\n");
                result.append("Updated At: ").append(user.getUpdatedAt()).append("\n");
                result.append("----------------------------------------\n");
            }

            return result.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    @GetMapping("/test-connection")
    public String testConnection() {
        try {
            long userCount = userService.countUsers();
            return "Database connection successful! Total users: " + userCount;
        } catch (Exception e) {
            return "Database connection failed: " + e.getMessage();
        }
    }
}