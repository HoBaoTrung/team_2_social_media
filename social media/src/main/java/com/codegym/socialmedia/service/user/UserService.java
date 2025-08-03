package com.codegym.socialmedia.service.user;


import com.codegym.socialmedia.dto.UserRegistrationDto;
import com.codegym.socialmedia.model.account.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User getCurrentUser();

    User getUserByUsername(String username);

    User save(User user);
    User save(User user, MultipartFile file);
    User save(UserRegistrationDto registrationDto);

    User findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    User createOrUpdateOAuth2User(String email, String name, String provider, String avatar);

    List<User> getAllUsers();

    long countUsers();

    void deleteAllUsers();
}
