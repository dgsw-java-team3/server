package com.todayschool.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.todayschool.domain.reservation.dto.RoomResponse;
import com.todayschool.domain.reservation.dto.RoomUpdateRequest;
import com.todayschool.domain.reservation.entity.Room;
import com.todayschool.domain.reservation.repository.ReservationRepository;
import com.todayschool.domain.reservation.repository.RoomRepository;
import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 20);

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Room room(String name) {
        return Room.builder().name(name).capacity(30).location("1층").build();
    }

    @Test
    @DisplayName("빈 호실 조회는 시작 시각이 종료 시각보다 늦거나 같으면 INVALID_RESERVATION_TIME 예외가 발생한다")
    void getAvailableRooms_invalidTimeOrder() {
        assertThatThrownBy(() -> roomService.getAvailableRooms(DATE, LocalTime.of(12, 0), LocalTime.of(12, 0)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_RESERVATION_TIME);
        verify(roomRepository, never()).findAvailableRooms(any(), any(), any());
    }

    @Test
    @DisplayName("빈 호실 조회는 Repository 결과를 응답 DTO로 변환한다")
    void getAvailableRooms_success() {
        given(roomRepository.findAvailableRooms(DATE, LocalTime.of(10, 0), LocalTime.of(12, 0)))
                .willReturn(List.of(room("A101")));

        List<RoomResponse> result = roomService.getAvailableRooms(DATE, LocalTime.of(10, 0), LocalTime.of(12, 0));

        assertThat(result).extracting(RoomResponse::name).containsExactly("A101");
    }

    @Test
    @DisplayName("호실 수정 시 다른 호실과 이름이 겹치면 ROOM_DUPLICATED 예외가 발생한다")
    void updateRoom_duplicatedName() {
        given(roomRepository.findById(1L)).willReturn(Optional.of(room("A101")));
        given(roomRepository.existsByNameAndIdNot("A102", 1L)).willReturn(true);

        assertThatThrownBy(() -> roomService.updateRoom(1L, new RoomUpdateRequest("A102", 30, "1층")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ROOM_DUPLICATED);
    }

    @Test
    @DisplayName("예약이 남은 호실을 삭제하면 ROOM_HAS_RESERVATIONS 예외가 발생하고 삭제하지 않는다")
    void deleteRoom_withReservations() {
        Room room = room("A101");
        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(reservationRepository.existsByRoomId(1L)).willReturn(true);

        assertThatThrownBy(() -> roomService.deleteRoom(1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ROOM_HAS_RESERVATIONS);
        verify(roomRepository, never()).delete(any());
    }

    @Test
    @DisplayName("예약이 없는 호실은 삭제된다")
    void deleteRoom_success() {
        Room room = room("A101");
        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(reservationRepository.existsByRoomId(1L)).willReturn(false);

        roomService.deleteRoom(1L);

        verify(roomRepository).delete(room);
    }
}
