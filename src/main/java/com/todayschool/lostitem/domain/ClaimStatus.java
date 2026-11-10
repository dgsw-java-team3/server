package com.todayschool.lostitem.domain;

/** 반환 신청 처리 상태입니다. */
public enum ClaimStatus {
    PENDING,  // 대기
    APPROVED, // 승인
    REJECTED  // 거절
}
