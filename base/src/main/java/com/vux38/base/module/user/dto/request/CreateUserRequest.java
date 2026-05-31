package com.vux38.base.module.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new user.
 *
 * <p>
 * Contains validated input data for user registration.
 * </p>
 *
 * <b>Security Notes:</b>
 * <ul>
 *     <li>Password must be encoded before saving</li>
 *     <li>Role is NOT accepted from client</li>
 * </ul>
 *
 * @author Vux38
 */
@Data
public class CreateUserRequest {

    /**
     * User email (must be unique)
     */
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    /**
     * Raw password (will be encoded)
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /**
     * Display name
     */
    @NotBlank(message = "Name is required")
    private String name;
}