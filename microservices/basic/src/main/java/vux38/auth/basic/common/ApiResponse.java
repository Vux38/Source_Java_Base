package vux38.auth.basic.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private int status;
    private boolean success;
    private String message;
    private T data;

    // Thành công có data
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // Thành công không cần data (vd: logout)
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .status(200)
                .success(true)
                .message(message)
                .data(null)
                .build();
    }

    // Thành công custom status (vd: 201 Created)
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // Thất bại
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .success(false)
                .message(message)
                .data(null)
                .build();
    }
}