package com.codegym.socialmedia.service.user;

import com.codegym.socialmedia.component.CloudinaryService;
import com.codegym.socialmedia.model.account.User;
import com.codegym.socialmedia.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private IUserRepository iUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
@Autowired
private CloudinaryService cloudinaryService;

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) auth.getPrincipal();
            return getUserByUsername(userDetails.getUsername());
        }

        return null;
    }

    @Override
    public User getUserByUsername(String username) {
        return iUserRepository.findByUsername(username);
    }


    @Override
    public User save(User newUser) {
        User user = getUserByUsername(newUser.getUsername());
        if (user != null) {
            if (user.getPasswordHash().equals(newUser.getPasswordHash())) {
                newUser.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));
            }
        }
        return iUserRepository.save(newUser);
    }

    @Override
    public User save(User user, MultipartFile image) {
        if (!image.isEmpty()) {
            user.setProfilePicture(cloudinaryService.upload(image));
        }
        return save(user);
    }

}
