package com.todayschool.domain.reservation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.todayschool.domain.reservation.entity.Reservation;
import com.todayschool.domain.reservation.entity.Room;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** data.sql도 함께 적재되므로, 테스트용 호실을 따로 만들어 그 호실 기준으로만 검증합니다. */
@DataJpaTest
class ReservationRepositoryTest {

    private static final LocalDate DATE = LocalDate.of(2026, 12, 1);

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    private Room room;
    private Reservation base;

    @BeforeEach
    void setUp() {
        room = roomRepository.save(Room.builder().name("테스트실").capacity(10).location("별관").build());
        // 기준 예약: 10:00 ~ 12:00
        base = reservationRepository.save(reservation(room, DATE, 10, 12));
    }

    private Reservation reservation(Room room, LocalDate date, int startHour, int endHour) {
        return Reservation.builder()
                .room(room)
                .reservationDate(date)
                .startTime(LocalTime.of(startHour, 0))
                .endTime(LocalTime.of(endHour, 0))
                .reserverName("학생1")
                .purpose("테스트")
                .build();
    }

    private boolean overlaps(Long roomId, LocalDate date, int startHour, int endHour, Long excludeId) {
        return reservationRepository.existsOverlapping(
                roomId, date, LocalTime.of(startHour, 0), LocalTime.of(endHour, 0), excludeId);
    }

    @Test
    @DisplayName("기존 예약과 일부가 겹치면 겹친다")
    void overlapping_partial() {
        assertThat(overlaps(room.getId(), DATE, 11, 13, null)).isTrue();
        assertThat(overlaps(room.getId(), DATE, 9, 11, null)).isTrue();
    }

    @Test
    @DisplayName("기존 예약을 완전히 포함하거나 그 안에 들어가도 겹친다")
    void overlapping_contains() {
        assertThat(overlaps(room.getId(), DATE, 9, 13, null)).isTrue();
        assertThat(overlaps(room.getId(), DATE, 10, 12, null)).isTrue();
        assertThat(overlaps(room.getId(), DATE, 10, 11, null)).isTrue();
    }

    @Test
    @DisplayName("맞닿는 경계(기존 종료 12:00, 새 시작 12:00)는 겹치지 않는다")
    void touchingBoundary_isNotOverlap() {
        assertThat(overlaps(room.getId(), DATE, 12, 14, null)).isFalse();
        assertThat(overlaps(room.getId(), DATE, 8, 10, null)).isFalse();
    }

    @Test
    @DisplayName("다른 날짜나 다른 호실이면 같은 시간이어도 겹치지 않는다")
    void differentDateOrRoom_isNotOverlap() {
        Room other = roomRepository.save(Room.builder().name("다른 테스트실").capacity(5).location("별관").build());

        assertThat(overlaps(room.getId(), DATE.plusDays(1), 10, 12, null)).isFalse();
        assertThat(overlaps(other.getId(), DATE, 10, 12, null)).isFalse();
    }

    @Test
    @DisplayName("본인 예약을 제외하면 같은 시간으로 변경해도 겹치지 않는다")
    void excludeId_ignoresItself() {
        assertThat(overlaps(room.getId(), DATE, 10, 12, base.getId())).isFalse();
        // 본인이 아닌 예약은 제외되지 않는다.
        assertThat(overlaps(room.getId(), DATE, 10, 12, base.getId() + 1000)).isTrue();
    }

    @Test
    @DisplayName("본인을 제외해도 다른 예약과 겹치면 겹친다")
    void excludeId_stillDetectsOtherReservation() {
        reservationRepository.save(reservation(room, DATE, 13, 15));

        assertThat(overlaps(room.getId(), DATE, 12, 14, base.getId())).isTrue();
    }

    @Test
    @DisplayName("빈 호실 조회는 겹치는 예약이 있는 호실을 제외한다 (NOT EXISTS)")
    void findAvailableRooms_excludesBookedRoom() {
        List<Room> available = roomRepository.findAvailableRooms(DATE, LocalTime.of(11, 0), LocalTime.of(13, 0));

        assertThat(available).extracting(Room::getId).doesNotContain(room.getId());
    }

    @Test
    @DisplayName("빈 호실 조회는 맞닿는 경계나 다른 날짜의 호실은 빈 것으로 본다")
    void findAvailableRooms_includesTouchingAndOtherDate() {
        List<Room> touching = roomRepository.findAvailableRooms(DATE, LocalTime.of(12, 0), LocalTime.of(14, 0));
        List<Room> otherDate = roomRepository.findAvailableRooms(
                DATE.plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0));

        assertThat(touching).extracting(Room::getId).contains(room.getId());
        assertThat(otherDate).extracting(Room::getId).contains(room.getId());
    }
}
