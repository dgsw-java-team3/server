package com.todayschool.domain.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/** 예약 변경 요청입니다. 호실과 예약자는 바꿀 수 없고, 목적(purpose)만 선택 값입니다. */
public record ReservationUpdateRequest(
        @NotNull LocalDate reservationDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        String purpose) {
}
