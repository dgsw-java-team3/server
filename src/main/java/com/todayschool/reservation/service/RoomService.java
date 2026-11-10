package com.todayschool.reservation.service;

import com.todayschool.reservation.dto.RoomCreateRequest;
import com.todayschool.reservation.dto.RoomResponse;

/** 호실 등록·조회 비즈니스 로직입니다. */
public interface RoomService {

    /** 호실을 등록합니다. 이름이 이미 있으면 ROOM_DUPLICATED 예외가 발생합니다. */
    RoomResponse createRoom(RoomCreateRequest request);
}
