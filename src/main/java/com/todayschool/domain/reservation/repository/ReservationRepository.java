package com.todayschool.domain.reservation.repository;

import com.todayschool.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

/** 예약 데이터 접근 계층입니다. */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 해당 호실에 예약이 하나라도 남아 있는지 확인합니다. */
    boolean existsByRoomId(Long roomId);
}
