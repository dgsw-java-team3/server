package com.todayschool.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/** 예약 생성 요청입니다. 목적(purpose)만 선택 값입니다. */
public record ReservationCreateRequest(
        @NotNull Long roomId,
        @NotNull LocalDate reservationDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @NotBlank String reserverName,
        String purpose) {
}
