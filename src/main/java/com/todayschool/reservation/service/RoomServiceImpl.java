package com.todayschool.reservation.service;

import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import com.todayschool.reservation.domain.Room;
import com.todayschool.reservation.dto.RoomCreateRequest;
import com.todayschool.reservation.dto.RoomResponse;
import com.todayschool.reservation.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** RoomService 구현체입니다. 트랜잭션은 이 계층에서 관리합니다. */
@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

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
}
