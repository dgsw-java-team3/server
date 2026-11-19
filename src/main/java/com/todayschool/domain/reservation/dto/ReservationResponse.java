package com.todayschool.domain.reservation.dto;

import com.todayschool.domain.reservation.entity.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;

/** 예약 응답입니다. 엔티티를 직접 노출하지 않기 위해 사용합니다. */
public record ReservationResponse(
        Long id,
        Long roomId,
        String roomName,
        LocalDate reservationDate,
        LocalTime startTime,
        LocalTime endTime,
        String reserverName,
        String purpose) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getRoom().getId(),
                reservation.getRoom().getName(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getReserverName(),
                reservation.getPurpose());
    }
}
