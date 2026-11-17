package com.todayschool.domain.reservation.repository;

import com.todayschool.domain.reservation.entity.Room;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** 호실 데이터 접근 계층입니다. */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /** 같은 이름의 호실이 있는지 확인합니다. */
    boolean existsByName(String name);

    /** 자기 자신을 제외하고 같은 이름의 호실이 있는지 확인합니다. */
    boolean existsByNameAndIdNot(String name, Long id);

    /**
     * 해당 날짜·시간대에 겹치는 예약이 하나도 없는 호실을 조회합니다.
     * 겹침 조건은 예약 겹침 검사와 같고, 맞닿는 경계는 빈 것으로 봅니다.
     */
    @Query("""
            select r from Room r
            where not exists (
                select 1 from Reservation v
                where v.room = r
                  and v.reservationDate = :date
                  and v.startTime < :endTime
                  and v.endTime > :startTime
            )
            order by r.id asc
            """)
    List<Room> findAvailableRooms(
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
}
