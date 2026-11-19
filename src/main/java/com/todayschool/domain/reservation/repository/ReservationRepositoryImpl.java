package com.todayschool.domain.reservation.repository;

import static com.todayschool.domain.reservation.entity.QReservation.reservation;
import static com.todayschool.domain.reservation.entity.QRoom.room;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.todayschool.domain.reservation.entity.Reservation;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;

/** Spring Data가 이름 규칙(~Impl)으로 찾아 ReservationRepository에 합쳐 줍니다. */
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /** 값이 없는 조건은 null을 돌려주고, where()는 null 조건을 빼고 AND로 묶습니다. */
    @Override
    public List<Reservation> search(Long roomId, LocalDate date) {
        return queryFactory
                .selectFrom(reservation)
                .join(reservation.room, room).fetchJoin()
                .where(roomIdEq(roomId), dateEq(date))
                .orderBy(reservation.reservationDate.asc(), reservation.startTime.asc())
                .fetch();
    }

    private BooleanExpression roomIdEq(Long roomId) {
        return roomId != null ? reservation.room.id.eq(roomId) : null;
    }

    private BooleanExpression dateEq(LocalDate date) {
        return date != null ? reservation.reservationDate.eq(date) : null;
    }
}
