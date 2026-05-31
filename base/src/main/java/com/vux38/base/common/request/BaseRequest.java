package com.vux38.base.common.request;

import lombok.Data;

@Data
public class BaseRequest<T> {

    private Meta meta;
    private T data;

    @Data
    public static class Meta {
        private String traceId;
        private String clientVersion;
        private String locale;
    }
}