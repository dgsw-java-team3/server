package com.todayschool.global.exception;

import lombok.Getter;

/** 서비스에서 예상 가능한 실패(대상 없음, 중복, 규칙 위반) 시 던집니다. */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
