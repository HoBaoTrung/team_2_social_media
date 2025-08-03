package com.codegym.socialmedia.dto;

import com.codegym.socialmedia.general_interface.NormalRegister;
import com.codegym.socialmedia.model.account.User;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {
    private String username;

    @Email(message = "Email không hợp lệ")
    @NotBlank
    private String email;

    @NotBlank(groups = NormalRegister.class, message = "Không để trống")
    @Pattern(regexp = "^(\\+84|0)(3[2-9]|5[6,8,9]|7[0,6-9]|8[1-5]|9[0-9])\\d{7}$"
            ,message = "Sai định dạng"
            ,groups = NormalRegister.class
    )
    private String phone;
    @Lob
    private String bio;
    private String avatarUrl;
    @Size(max = 50)
    private String firstName;
    @Size(max = 50)
    private String lasttName;
    public UserUpdateDto (User user){
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.bio = user.getBio();
        this.avatarUrl = user.getProfilePicture();
        this.firstName = user.getFirstName();
        this.lasttName = user.getLastName();
    }

    public User toUser(User user) {
        user.setUsername(this.username);
        user.setEmail(this.email);
        user.setPhone(this.phone);
        user.setBio(this.bio);
        user.setProfilePicture(this.avatarUrl);
        user.setFirstName(this.firstName);
        user.setLastName(this.lasttName);
        return user;
    }
}
