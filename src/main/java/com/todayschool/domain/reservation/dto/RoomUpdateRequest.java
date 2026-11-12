package com.todayschool.domain.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** 호실 수정 요청입니다. 수용 인원은 1 이상이어야 합니다. */
public record RoomUpdateRequest(
        @NotBlank String name,
        @Min(1) int capacity,
        @NotBlank String location) {
}
