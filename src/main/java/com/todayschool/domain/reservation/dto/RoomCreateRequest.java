package com.todayschool.domain.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** 호실 등록 요청입니다. 수용 인원은 1 이상이어야 합니다. */
public record RoomCreateRequest(
        @NotBlank @Schema(example = "실습실 101") String name,
        @Min(1) @Schema(example = "30") int capacity,
        @NotBlank @Schema(example = "본관 1층") String location) {
}
