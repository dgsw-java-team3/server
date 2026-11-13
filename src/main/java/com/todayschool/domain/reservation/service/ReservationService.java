package com.todayschool.domain.reservation.service;

import com.todayschool.domain.reservation.dto.ReservationCreateRequest;
import com.todayschool.domain.reservation.dto.ReservationResponse;
import java.time.LocalDate;
import java.util.List;

/** 예약 비즈니스 로직입니다. */
public interface ReservationService {

    /**
     * 예약을 생성합니다. 시작 시각이 종료 시각보다 늦거나 같으면 INVALID_RESERVATION_TIME,
     * 호실이 없으면 ROOM_NOT_FOUND, 시간이 겹치면 RESERVATION_CONFLICT 예외가 발생합니다.
     */
    ReservationResponse createReservation(ReservationCreateRequest request);

    /** 예약 현황을 조회합니다. 호실·날짜는 모두 선택 조건이며, 없으면 전체를 조회합니다. */
    List<ReservationResponse> getReservations(Long roomId, LocalDate date);

    /** 예약을 취소(삭제)합니다. 없으면 RESERVATION_NOT_FOUND 예외가 발생합니다. */
    void cancelReservation(Long reservationId);
}
