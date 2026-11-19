package com.todayschool.domain.reservation.entity;

import com.todayschool.global.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 예약 대상 실습실입니다. 이름은 중복될 수 없습니다. */
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false, length = 100)
    private String location;

    @Builder
    public Room(String name, int capacity, String location) {
        this.name = name;
        this.capacity = capacity;
        this.location = location;
    }

    /** 호실 정보를 수정합니다. */
    public void update(String name, int capacity, String location) {
        this.name = name;
        this.capacity = capacity;
        this.location = location;
    }
}
