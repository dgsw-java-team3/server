package com.todayschool.domain.reservation.repository;

import com.todayschool.domain.reservation.entity.Reservation;
import java.time.LocalDate;
import java.util.List;

/** QueryDSL로 구현하는 예약 조회입니다. */
public interface ReservationRepositoryCustom {

    /** 호실·날짜 조건으로 예약을 조회합니다. 조건은 모두 선택이며, 날짜·시작 시각 순으로 정렬합니다. */
    List<Reservation> search(Long roomId, LocalDate date);
}
