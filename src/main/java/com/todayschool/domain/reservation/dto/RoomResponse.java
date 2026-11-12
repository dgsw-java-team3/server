package com.todayschool.domain.reservation.dto;

import com.todayschool.domain.reservation.entity.Room;

/** 호실 응답입니다. 엔티티를 직접 노출하지 않기 위해 사용합니다. */
public record RoomResponse(Long id, String name, int capacity, String location) {

    public static RoomResponse from(Room room) {
        return new RoomResponse(room.getId(), room.getName(), room.getCapacity(), room.getLocation());
    }
}
