package com.todayschool.domain.reservation.repository;

import com.todayschool.domain.reservation.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

/** 호실 데이터 접근 계층입니다. */
public interface RoomRepository extends JpaRepository<Room, Long> {

    /** 같은 이름의 호실이 있는지 확인합니다. */
    boolean existsByName(String name);

    /** 자기 자신을 제외하고 같은 이름의 호실이 있는지 확인합니다. */
    boolean existsByNameAndIdNot(String name, Long id);
}
