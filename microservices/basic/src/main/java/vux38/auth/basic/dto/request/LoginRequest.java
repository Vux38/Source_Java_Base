package vux38.auth.basic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import vux38.auth.basic.entity.AuthProvider;

// dto/request/LoginRequest.java
@Getter
public class LoginRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    private AuthProvider provider = AuthProvider.LOCAL; // mặc định LOCAL
}