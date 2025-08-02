package com.codegym.socialmedia.service.user;


import com.codegym.socialmedia.model.account.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    User getCurrentUser();

    User getUserByUsername(String username);

    User save(User user);
    User save(User user, MultipartFile file);
}
