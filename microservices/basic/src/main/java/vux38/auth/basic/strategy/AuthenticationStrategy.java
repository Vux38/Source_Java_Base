package vux38.auth.basic.strategy;

import vux38.auth.basic.dto.request.LoginRequest;
import vux38.auth.basic.dto.response.AuthResponse;
import vux38.auth.basic.entity.AuthProvider;

public interface AuthenticationStrategy {

    /**
     * Strategy này có xử lý provider này không?
     * VD: PasswordAuthStrategy → supports(LOCAL) = true
     */
    boolean supports(AuthProvider provider);

    /**
     * Thực hiện xác thực và trả về token
     */
    AuthResponse authenticate(LoginRequest request);
}