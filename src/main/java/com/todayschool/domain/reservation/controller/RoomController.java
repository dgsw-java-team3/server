package com.todayschool.domain.reservation.controller;

import com.todayschool.global.response.ApiResponse;
import com.todayschool.domain.reservation.dto.RoomCreateRequest;
import com.todayschool.domain.reservation.dto.RoomResponse;
import com.todayschool.domain.reservation.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 호실 등록·조회 요청을 받아 응답 형식으로 감싸 돌려줍니다. */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    /** B-1 호실 등록. 성공하면 201을 응답합니다. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RoomResponse> createRoom(@Valid @RequestBody RoomCreateRequest request) {
        return ApiResponse.ok(roomService.createRoom(request));
    }

    /** B-2 호실 목록 조회. */
    @GetMapping
    public ApiResponse<List<RoomResponse>> getRooms() {
        return ApiResponse.ok(roomService.getRooms());
    }
}
