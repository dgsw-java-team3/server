package com.todayschool.domain.reservation.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todayschool.domain.reservation.dto.RoomCreateRequest;
import com.todayschool.domain.reservation.dto.RoomResponse;
import com.todayschool.domain.reservation.dto.RoomUpdateRequest;
import com.todayschool.domain.reservation.service.RoomService;
import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import com.todayschool.global.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RoomController.class)
@Import(GlobalExceptionHandler.class)
class RoomControllerTest {

    private static final String VALID_BODY = """
            {"name": "A101", "capacity": 30, "location": "1층"}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RoomService roomService;

    @Test
    @DisplayName("호실을 등록하면 201과 응답 틀로 감싼 호실을 반환한다")
    void createRoom_returns201() throws Exception {
        given(roomService.createRoom(any(RoomCreateRequest.class)))
                .willReturn(new RoomResponse(1L, "A101", 30, "1층"));

        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("A101"));
    }

    @Test
    @DisplayName("수용 인원이 0이면 400과 INVALID_INPUT_VALUE를 반환하고 서비스는 호출하지 않는다")
    void createRoom_invalidCapacity_returns400() throws Exception {
        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "A101", "capacity": 0, "location": "1층"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        verify(roomService, never()).createRoom(any());
    }

    @Test
    @DisplayName("호실 이름이 중복이면 409와 ROOM_DUPLICATED를 반환한다")
    void createRoom_duplicated_returns409() throws Exception {
        given(roomService.createRoom(any(RoomCreateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ROOM_DUPLICATED));

        mvc.perform(post("/api/rooms").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ROOM_DUPLICATED"));
    }

    @Test
    @DisplayName("호실 목록을 조회하면 200을 반환한다")
    void getRooms_returns200() throws Exception {
        given(roomService.getRooms()).willReturn(List.of(new RoomResponse(1L, "A101", 30, "1층")));

        mvc.perform(get("/api/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("A101"));
    }

    @Test
    @DisplayName("없는 호실을 수정하면 404와 ROOM_NOT_FOUND를 반환한다")
    void updateRoom_notFound_returns404() throws Exception {
        given(roomService.updateRoom(eq(99L), any(RoomUpdateRequest.class)))
                .willThrow(new BusinessException(ErrorCode.ROOM_NOT_FOUND));

        mvc.perform(put("/api/rooms/99").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("ROOM_NOT_FOUND"));
    }

    @Test
    @DisplayName("호실을 삭제하면 200과 data null을 반환한다")
    void deleteRoom_returns200() throws Exception {
        mvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()));
        verify(roomService).deleteRoom(1L);
    }

    @Test
    @DisplayName("예약이 남은 호실을 삭제하면 409와 ROOM_HAS_RESERVATIONS를 반환한다")
    void deleteRoom_withReservations_returns409() throws Exception {
        doThrow(new BusinessException(ErrorCode.ROOM_HAS_RESERVATIONS)).when(roomService).deleteRoom(1L);

        mvc.perform(delete("/api/rooms/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("ROOM_HAS_RESERVATIONS"));
    }

    @Test
    @DisplayName("빈 호실 조회는 /available 경로로 매핑되어 200을 반환한다")
    void getAvailableRooms_returns200() throws Exception {
        given(roomService.getAvailableRooms(
                LocalDate.of(2026, 11, 20), LocalTime.of(10, 0), LocalTime.of(12, 0)))
                .willReturn(List.of(new RoomResponse(4L, "실습실 202", 20, "본관 2층")));

        mvc.perform(get("/api/rooms/available")
                        .param("date", "2026-11-20")
                        .param("startTime", "10:00")
                        .param("endTime", "12:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(4));
    }

    @Test
    @DisplayName("빈 호실 조회에서 날짜 형식이 틀리거나 필수 값이 빠지면 400을 반환한다")
    void getAvailableRooms_invalidParams_returns400() throws Exception {
        mvc.perform(get("/api/rooms/available")
                        .param("date", "abc").param("startTime", "10:00").param("endTime", "12:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));

        mvc.perform(get("/api/rooms/available").param("date", "2026-11-20").param("startTime", "10:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }
}
