package com.todayschool.global.response;

/** 성공 응답입니다. 삭제처럼 반환할 데이터가 없으면 ok(null, 메시지)를 사용합니다. */
public record ApiResponse<T>(boolean success, T data, String message) {

    public static <T> ApiResponse<T> ok(T data) {
        return ok(data, "요청이 성공했습니다.");
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
}
