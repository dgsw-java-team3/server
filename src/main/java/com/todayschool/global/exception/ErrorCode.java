package com.todayschool.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/** 에러 코드 목록입니다. 도메인별 코드는 각 구역 주석 아래에 추가합니다. */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 경로를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 HTTP 메서드입니다."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 Content-Type입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // A. 급식 만족도
    MEAL_NOT_FOUND(HttpStatus.NOT_FOUND, "급식을 찾을 수 없습니다."),
    DUPLICATE_MEAL(HttpStatus.CONFLICT, "해당 날짜와 구분의 급식이 이미 있습니다."),

    // B. 실습실 예약

    // C. 분실물 게시판
    LOST_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "습득물을 찾을 수 없습니다."),
    LOST_ITEM_ALREADY_RETURNED(HttpStatus.CONFLICT, "이미 반환 완료된 습득물입니다."),
    ;

    private final HttpStatus status;
    private final String message;
}
