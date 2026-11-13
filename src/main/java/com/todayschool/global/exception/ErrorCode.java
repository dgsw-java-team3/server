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
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    // A. 급식 만족도

    // B. 실습실 예약
    ROOM_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 호실 이름입니다."),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "호실을 찾을 수 없습니다."),
    ROOM_HAS_RESERVATIONS(HttpStatus.CONFLICT, "예약이 남아 있는 호실은 삭제할 수 없습니다."),
    INVALID_RESERVATION_TIME(HttpStatus.BAD_REQUEST, "시작 시각은 종료 시각보다 빨라야 합니다."),
    RESERVATION_CONFLICT(HttpStatus.CONFLICT, "이미 예약된 시간입니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),

    // C. 분실물 게시판
    ;

    private final HttpStatus status;
    private final String message;
}
