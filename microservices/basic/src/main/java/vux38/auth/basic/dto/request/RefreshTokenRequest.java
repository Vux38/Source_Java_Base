package vux38.auth.basic.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

// dto/request/RefreshTokenRequest.java
@Getter
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token không được để trống")
    private String refreshToken;
}