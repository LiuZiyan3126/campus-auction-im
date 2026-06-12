package com.ca.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> {

    private int code;
    private String message;
    private T data;
    private long timestamp;

    public static <T> ResultVO<T> ok(T data) {
        return new ResultVO<>(200, "操作成功", data, Instant.now().toEpochMilli());
    }

    public static <T> ResultVO<T> ok(String message, T data) {
        return new ResultVO<>(200, message, data, Instant.now().toEpochMilli());
    }

    public static <T> ResultVO<T> fail(int code, String message) {
        return new ResultVO<>(code, message, null, Instant.now().toEpochMilli());
    }

    public static <T> ResultVO<T> fail(String message) {
        return new ResultVO<>(400, message, null, Instant.now().toEpochMilli());
    }
}
