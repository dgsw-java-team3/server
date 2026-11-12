package com.todayschool.domain.reservation.service;

import com.todayschool.domain.reservation.dto.ReservationCreateRequest;
import com.todayschool.domain.reservation.dto.ReservationResponse;

/** 예약 비즈니스 로직입니다. */
public interface ReservationService {

    /**
     * 예약을 생성합니다. 시작 시각이 종료 시각보다 늦거나 같으면 INVALID_RESERVATION_TIME,
     * 호실이 없으면 ROOM_NOT_FOUND 예외가 발생합니다.
     */
    ReservationResponse createReservation(ReservationCreateRequest request);
}
