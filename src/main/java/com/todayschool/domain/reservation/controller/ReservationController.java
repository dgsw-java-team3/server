package com.todayschool.domain.reservation.controller;

import com.todayschool.domain.reservation.dto.ReservationCreateRequest;
import com.todayschool.domain.reservation.dto.ReservationResponse;
import com.todayschool.domain.reservation.dto.ReservationUpdateRequest;
import com.todayschool.domain.reservation.service.ReservationService;
import com.todayschool.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 예약 요청을 받아 응답 형식으로 감싸 돌려줍니다. */
@Tag(name = "실습실 예약")
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /** B-5 예약 생성. 성공하면 201을 응답합니다. */
    @Operation(summary = "예약 생성",
            description = "같은 호실·날짜에서 시간이 겹치면 409입니다. 맞닿는 경계는 허용합니다. 시작 ≥ 종료이면 400입니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request) {
        return ApiResponse.ok(reservationService.createReservation(request));
    }

    /** B-6 예약 현황 조회. 호실·날짜(yyyy-MM-dd)는 모두 선택 조건입니다. */
    @Operation(summary = "예약 현황 조회", description = "호실·날짜 조건은 모두 선택이며, 날짜·시작 시각 순입니다.")
    @GetMapping
    public ApiResponse<List<ReservationResponse>> getReservations(
            @Parameter(example = "1") @RequestParam(required = false) Long roomId,
            @Parameter(example = "2026-11-20") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(reservationService.getReservations(roomId, date));
    }

    /** B-8 예약 취소. 성공하면 200과 data null을 응답합니다. */
    @Operation(summary = "예약 취소")
    @DeleteMapping("/{reservationId}")
    public ApiResponse<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ApiResponse.ok(null, "예약이 취소되었습니다.");
    }

    /** B-7 예약 변경. 날짜, 시간, 목적을 바꿉니다. */
    @Operation(summary = "예약 변경", description = "날짜, 시간, 목적을 바꿉니다. 겹침 검사에서 본인 예약은 제외합니다.")
    @PutMapping("/{reservationId}")
    public ApiResponse<ReservationResponse> updateReservation(
            @PathVariable Long reservationId, @Valid @RequestBody ReservationUpdateRequest request) {
        return ApiResponse.ok(reservationService.updateReservation(reservationId, request));
    }
}
