package com.todayschool.domain.reservation.repository;

import com.todayschool.domain.reservation.entity.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 예약 데이터 접근 계층입니다. */
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** 해당 호실에 예약이 하나라도 남아 있는지 확인합니다. */
    boolean existsByRoomId(Long roomId);

    /**
     * 같은 호실·같은 날짜에 시간이 겹치는 예약이 있는지 확인합니다.
     * 기존.start < 새.end 그리고 기존.end > 새.start 이면 겹치며, 맞닿는 경계(10:00 종료/10:00 시작)는 겹치지 않습니다.
     */
    @Query("""
            select count(r) > 0 from Reservation r
            where r.room.id = :roomId
              and r.reservationDate = :date
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    boolean existsOverlapping(
            @Param("roomId") Long roomId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);

    /**
     * 호실·날짜 조건으로 예약을 조회합니다. 조건은 모두 선택이며, null이면 그 조건은 적용하지 않습니다.
     * 날짜, 시작 시각 순으로 정렬합니다.
     */
    @Query("""
            select r from Reservation r join fetch r.room
            where (:roomId is null or r.room.id = :roomId)
              and (:date is null or r.reservationDate = :date)
            order by r.reservationDate asc, r.startTime asc
            """)
    List<Reservation> findAllByCondition(@Param("roomId") Long roomId, @Param("date") LocalDate date);
}
