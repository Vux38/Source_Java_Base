package com.vux38.auth.client;

import com.vux38.auth.dto.request.CreateUserProfileRequest;
import com.vux38.common.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for communicating with User Service.
 * <p>
 * This client is used to call user-service APIs from auth-service
 * without creating direct coupling between the two microservices.
 * </p>
 *
 * @author VUX38
 * @version 1.0
 * @since 2026
 */
@FeignClient(
        name = "user-service",
        url = "${user.service.url:http://localhost:8082}"
)
public interface UserServiceClient {

    /**
     * Create a user profile in user-service.
     *
     * @param request the user profile creation request
     * @return API response with void data
     */
    @PostMapping("/api/internal/users/profiles")
    ApiResponse<Void> createUserProfile(@RequestBody CreateUserProfileRequest request);
}