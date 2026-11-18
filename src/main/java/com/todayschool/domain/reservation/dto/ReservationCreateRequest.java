package com.todayschool.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/** 예약 생성 요청입니다. 목적(purpose)만 선택 값입니다. */
public record ReservationCreateRequest(
        @NotNull @Schema(example = "1") Long roomId,
        @NotNull @Schema(example = "2026-11-20") LocalDate reservationDate,
        @NotNull @Schema(type = "string", example = "12:00") LocalTime startTime,
        @NotNull @Schema(type = "string", example = "13:00") LocalTime endTime,
        @NotBlank @Schema(example = "학생1") String reserverName,
        @Schema(example = "자바 스터디") String purpose) {
}
