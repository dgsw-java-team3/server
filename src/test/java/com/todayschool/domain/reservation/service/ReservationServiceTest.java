package com.todayschool.domain.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.todayschool.domain.reservation.dto.ReservationCreateRequest;
import com.todayschool.domain.reservation.dto.ReservationResponse;
import com.todayschool.domain.reservation.entity.Reservation;
import com.todayschool.domain.reservation.entity.Room;
import com.todayschool.domain.reservation.repository.ReservationRepository;
import com.todayschool.domain.reservation.repository.RoomRepository;
import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final Long ROOM_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 20);

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private ReservationCreateRequest request(LocalTime start, LocalTime end) {
        return new ReservationCreateRequest(ROOM_ID, DATE, start, end, "학생1", "스터디");
    }

    private Room room() {
        return Room.builder().name("A101").capacity(30).location("1층").build();
    }

    @Test
    @DisplayName("겹치지 않는 시간에 예약하면 저장된다")
    void createReservation_success() {
        given(roomRepository.findById(ROOM_ID)).willReturn(Optional.of(room()));
        given(reservationRepository.existsOverlapping(any(), any(), any(), any())).willReturn(false);
        given(reservationRepository.save(any(Reservation.class))).willAnswer(inv -> inv.getArgument(0));

        ReservationResponse response =
                reservationService.createReservation(request(LocalTime.of(10, 0), LocalTime.of(12, 0)));

        assertThat(response.roomName()).isEqualTo("A101");
        assertThat(response.startTime()).isEqualTo(LocalTime.of(10, 0));
        assertThat(response.endTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(response.reserverName()).isEqualTo("학생1");
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    @DisplayName("겹치는 시간에 예약하면 RESERVATION_CONFLICT 예외가 발생하고 저장되지 않는다")
    void createReservation_overlap_throwsConflict() {
        LocalTime start = LocalTime.of(11, 0);
        LocalTime end = LocalTime.of(13, 0);
        given(roomRepository.findById(ROOM_ID)).willReturn(Optional.of(room()));
        given(reservationRepository.existsOverlapping(ROOM_ID, DATE, start, end)).willReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(request(start, end)))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.RESERVATION_CONFLICT);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("시작 시각이 종료 시각보다 늦거나 같으면 INVALID_RESERVATION_TIME 예외가 발생한다")
    void createReservation_invalidTime_throwsException() {
        assertThatThrownBy(() ->
                reservationService.createReservation(request(LocalTime.of(12, 0), LocalTime.of(12, 0))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_RESERVATION_TIME);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("없는 호실이면 ROOM_NOT_FOUND 예외가 발생한다")
    void createReservation_roomNotFound_throwsException() {
        given(roomRepository.findById(ROOM_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                reservationService.createReservation(request(LocalTime.of(10, 0), LocalTime.of(12, 0))))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.ROOM_NOT_FOUND);
    }
}
