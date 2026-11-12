package com.todayschool.domain.reservation.repository;

import com.todayschool.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

/** 예약 데이터 접근 계층입니다. */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
