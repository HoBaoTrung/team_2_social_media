package com.codegym.socialmedia.dto;

import com.codegym.socialmedia.general_interface.NormalRegister;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDto {

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải có từ 3-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 32, message = "Mật khẩu phải có từ 6-32 ký tự")
    private String password;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(groups = NormalRegister.class, message = "Không để trống")
    @Pattern(regexp = "^(\\+84|0)(3[2-9]|5[6,8,9]|7[0,6-9]|8[1-5]|9[0-9])\\d{7}$"
            ,message = "Sai định dạng"
            ,groups = NormalRegister.class
    )
    private String phone;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    private String firstName;
    private String lastName;


}