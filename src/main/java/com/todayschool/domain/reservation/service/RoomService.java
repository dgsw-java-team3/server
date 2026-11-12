package com.todayschool.domain.reservation.service;

import com.todayschool.domain.reservation.dto.RoomCreateRequest;
import com.todayschool.domain.reservation.dto.RoomResponse;
import com.todayschool.domain.reservation.dto.RoomUpdateRequest;
import java.util.List;

/** 호실 등록·조회 비즈니스 로직입니다. */
public interface RoomService {

    /** 호실을 등록합니다. 이름이 이미 있으면 ROOM_DUPLICATED 예외가 발생합니다. */
    RoomResponse createRoom(RoomCreateRequest request);

    /** 등록된 모든 호실을 조회합니다. */
    List<RoomResponse> getRooms();

    /** 호실을 수정합니다. 없으면 ROOM_NOT_FOUND, 이름이 겹치면 ROOM_DUPLICATED 예외가 발생합니다. */
    RoomResponse updateRoom(Long roomId, RoomUpdateRequest request);
}
