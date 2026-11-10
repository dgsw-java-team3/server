package com.todayschool.global.response;

import com.todayschool.global.exception.ErrorCode;

/** 실패 응답입니다. */
public record ErrorResponse(boolean success, ErrorDetail error) {

    public record ErrorDetail(String code, String message) {
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return of(errorCode, errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(false, new ErrorDetail(errorCode.name(), message));
    }
}
