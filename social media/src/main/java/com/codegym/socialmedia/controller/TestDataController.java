package com.codegym.socialmedia.controller;

import com.codegym.socialmedia.dto.UserRegistrationDto;
import com.codegym.socialmedia.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/test")
public class TestDataController {

    @Autowired
    private UserService userService;

    @GetMapping("/create-sample-users")
    public String createSampleUsers() {
        try {
            // Tạo user test 1
            if (!userService.existsByUsername("testuser1")) {
                UserRegistrationDto user1 = new UserRegistrationDto();
                user1.setUsername("testuser1");
                user1.setEmail("test1@example.com");
                user1.setPassword("123456");
                user1.setConfirmPassword("123456");
                user1.setFirstName("Test");
                user1.setLastName("User One");
                user1.setPhone("0123456789");
                user1.setDateOfBirth(LocalDate.of(1990, 1, 1));

                userService.save(user1);
            }

            // Tạo user test 2
            if (!userService.existsByUsername("testuser2")) {
                UserRegistrationDto user2 = new UserRegistrationDto();
                user2.setUsername("testuser2");
                user2.setEmail("test2@example.com");
                user2.setPassword("123456");
                user2.setConfirmPassword("123456");
                user2.setFirstName("Test");
                user2.setLastName("User Two");
                user2.setPhone("0987654321");
                user2.setDateOfBirth(LocalDate.of(1995, 5, 15));

                userService.save(user2);
            }

            return "Sample users created successfully!";

        } catch (Exception e) {
            return "Error creating sample users: " + e.getMessage();
        }
    }

    @GetMapping("/clear-users")
    public String clearAllUsers() {
        try {
            userService.deleteAllUsers();
            return "All users deleted successfully!";
        } catch (Exception e) {
            return "Error deleting users: " + e.getMessage();
        }
    }
}