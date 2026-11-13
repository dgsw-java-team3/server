package com.todayschool.domain.reservation.service;

import com.todayschool.domain.reservation.dto.ReservationCreateRequest;
import com.todayschool.domain.reservation.dto.ReservationResponse;
import com.todayschool.domain.reservation.entity.Reservation;
import com.todayschool.domain.reservation.entity.Room;
import com.todayschool.domain.reservation.repository.ReservationRepository;
import com.todayschool.domain.reservation.repository.RoomRepository;
import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** ReservationService 구현체입니다. 트랜잭션은 이 계층에서 관리합니다. */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public ReservationResponse createReservation(ReservationCreateRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessException(ErrorCode.INVALID_RESERVATION_TIME);
        }
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        // 같은 호실·날짜에서 시간이 겹치면 저장하지 않습니다.
        if (reservationRepository.existsOverlapping(
                request.roomId(), request.reservationDate(), request.startTime(), request.endTime())) {
            throw new BusinessException(ErrorCode.RESERVATION_CONFLICT);
        }
        Reservation reservation = Reservation.builder()
                .room(room)
                .reservationDate(request.reservationDate())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .reserverName(request.reserverName())
                .purpose(request.purpose())
                .build();
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservations(Long roomId, LocalDate date) {
        return reservationRepository.findAllByCondition(roomId, date).stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESERVATION_NOT_FOUND));
        reservationRepository.delete(reservation);
    }
}
