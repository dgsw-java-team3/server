package com.todayschool.domain.reservation.controller;

import com.todayschool.domain.reservation.dto.ReservationCreateRequest;
import com.todayschool.domain.reservation.dto.ReservationResponse;
import com.todayschool.domain.reservation.service.ReservationService;
import com.todayschool.global.response.ApiResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 예약 요청을 받아 응답 형식으로 감싸 돌려줍니다. */
@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    /** B-5 예약 생성. 성공하면 201을 응답합니다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest request) {
        return ApiResponse.ok(reservationService.createReservation(request));
    }

    /** B-6 예약 현황 조회. 호실·날짜(yyyy-MM-dd)는 모두 선택 조건입니다. */
    @GetMapping
    public ApiResponse<List<ReservationResponse>> getReservations(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.ok(reservationService.getReservations(roomId, date));
    }

    /** B-8 예약 취소. 성공하면 200과 data null을 응답합니다. */
    @DeleteMapping("/{reservationId}")
    public ApiResponse<Void> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ApiResponse.ok(null, "예약이 취소되었습니다.");
    }
}
