package com.todayschool.domain.reservation.service;

import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import com.todayschool.domain.reservation.entity.Room;
import com.todayschool.domain.reservation.dto.RoomCreateRequest;
import com.todayschool.domain.reservation.dto.RoomResponse;
import com.todayschool.domain.reservation.dto.RoomUpdateRequest;
import com.todayschool.domain.reservation.repository.ReservationRepository;
import com.todayschool.domain.reservation.repository.RoomRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RoomService 구현체입니다. 트랜잭션은 이 계층에서 관리합니다. */
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    @Override
    @Transactional
    public RoomResponse createRoom(RoomCreateRequest request) {
        // 이름 중복은 저장 전에 먼저 검사합니다.
        if (roomRepository.existsByName(request.name())) {
            throw new BusinessException(ErrorCode.ROOM_DUPLICATED);
        }
        Room room = Room.builder()
                .name(request.name())
                .capacity(request.capacity())
                .location(request.location())
                .build();
        return RoomResponse.from(roomRepository.save(room));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getRooms() {
        return roomRepository.findAll().stream().map(RoomResponse::from).toList();
    }

    @Override
    @Transactional
    public RoomResponse updateRoom(Long roomId, RoomUpdateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        // 본인을 제외한 다른 호실과 이름이 겹치는지 검사합니다.
        if (roomRepository.existsByNameAndIdNot(request.name(), roomId)) {
            throw new BusinessException(ErrorCode.ROOM_DUPLICATED);
        }
        room.update(request.name(), request.capacity(), request.location());
        return RoomResponse.from(room);
    }

    @Override
    @Transactional
    public void deleteRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        if (reservationRepository.existsByRoomId(roomId)) {
            throw new BusinessException(ErrorCode.ROOM_HAS_RESERVATIONS);
        }
        roomRepository.delete(room);
    }
}
