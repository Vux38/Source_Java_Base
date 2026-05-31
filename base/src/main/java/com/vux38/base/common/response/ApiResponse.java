package com.vux38.base.common.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private Meta meta;     // thông tin chung
    private T data;        // dữ liệu chính
    private ErrorBody error; // lỗi (nếu có)
    private Object extra;  // mở rộng (optional)

    @Data
    @Builder
    public static class Meta {
        private int status;
        private String message;
        private String traceId;
        private long timestamp;
    }

    @Data
    @Builder
    public static class ErrorBody {
        private String code;
        private String detail;
        private Object validationErrors;
    }
}