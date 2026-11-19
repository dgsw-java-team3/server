package com.todayschool.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

/** 예약 변경 요청입니다. 호실과 예약자는 바꿀 수 없고, 목적(purpose)만 선택 값입니다. */
public record ReservationUpdateRequest(
        @NotNull @Schema(example = "2026-11-20") LocalDate reservationDate,
        @NotNull @Schema(type = "string", example = "12:00") LocalTime startTime,
        @NotNull @Schema(type = "string", example = "13:00") LocalTime endTime,
        @Schema(example = "팀 프로젝트 회의") String purpose) {
}
