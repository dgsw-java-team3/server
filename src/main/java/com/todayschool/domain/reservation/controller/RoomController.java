package com.todayschool.domain.reservation.controller;

import com.todayschool.domain.reservation.dto.RoomCreateRequest;
import com.todayschool.domain.reservation.dto.RoomResponse;
import com.todayschool.domain.reservation.dto.RoomUpdateRequest;
import com.todayschool.domain.reservation.service.RoomService;
import com.todayschool.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
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

/** 호실 등록·조회 요청을 받아 응답 형식으로 감싸 돌려줍니다. */
@Tag(name = "실습실 호실")
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /** B-1 호실 등록. 성공하면 201을 응답합니다. */
    @Operation(summary = "호실 등록", description = "호실 이름은 중복될 수 없습니다(409). 수용 인원은 1 이상입니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return ApiResponse.ok(roomService.createRoom(request));
    }

    /** B-2 호실 목록 조회. */
    @Operation(summary = "호실 목록 조회")
    @GetMapping
    public ApiResponse<List<RoomResponse>> getRooms() {
        return ApiResponse.ok(roomService.getRooms());
    }

    /** B-3 호실 수정. */
    @Operation(summary = "호실 수정", description = "다른 호실과 이름이 겹치면 409입니다.")
    @PutMapping("/{roomId}")
    public ApiResponse<RoomResponse> updateRoom(
            @PathVariable Long roomId, @Valid @RequestBody RoomUpdateRequest request) {
        return ApiResponse.ok(roomService.updateRoom(roomId, request));
    }

    /** B-4 호실 삭제. 성공하면 200과 data null을 응답합니다. */
    @Operation(summary = "호실 삭제", description = "예약이 남아 있는 호실은 삭제할 수 없습니다(409).")
    @DeleteMapping("/{roomId}")
    public ApiResponse<Void> deleteRoom(@PathVariable Long roomId) {
        roomService.deleteRoom(roomId);
        return ApiResponse.ok(null, "호실이 삭제되었습니다.");
    }

    /** B-9 빈 호실 조회. 날짜(yyyy-MM-dd)와 시작·종료 시각(HH:mm)은 모두 필수입니다. */
    @Operation(summary = "빈 호실 조회",
            description = "해당 날짜·시간대에 겹치는 예약이 없는 호실입니다. 맞닿는 경계(10:00 종료/10:00 시작)는 빈 것으로 봅니다.")
    @GetMapping("/available")
    public ApiResponse<List<RoomResponse>> getAvailableRooms(
            @Parameter(example = "2026-11-20") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(example = "10:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @Parameter(example = "12:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime) {
        return ApiResponse.ok(roomService.getAvailableRooms(date, startTime, endTime));
    }
}
