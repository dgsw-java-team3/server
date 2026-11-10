package com.todayschool.domain.meal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todayschool.domain.meal.controller.MealController;
import com.todayschool.domain.meal.dto.MealResponse;
import com.todayschool.domain.meal.entity.MealType;
import com.todayschool.domain.meal.service.MealService;
import com.todayschool.global.exception.BusinessException;
import com.todayschool.global.exception.ErrorCode;
import com.todayschool.global.exception.GlobalExceptionHandler;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MealController.class)
@Import(GlobalExceptionHandler.class)
class MealControllerTest {

    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private MealService mealService;

    @Test
    @DisplayName("급식 등록 시 201과 생성 경로를 반환한다")
    void createMealReturns201AndLocation() throws Exception {
        given(mealService.createMeal(any())).willReturn(new MealResponse(1L, LocalDate.of(2026, 11, 10),
                MealType.LUNCH, "쌀밥, 불고기", LocalDateTime.now(), LocalDateTime.now()));
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-10","mealType":"LUNCH","menu":"쌀밥, 불고기"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/meals/1"))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("메뉴가 비어 있으면 400을 반환한다")
    void blankMenuReturns400WithFieldDetails() throws Exception {
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-10","mealType":"LUNCH","menu":" "}
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.error.message", containsString("menu")));
        verifyNoInteractions(mealService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/meals?date=wrong", "/api/meals?size=101", "/api/meals?page=-1",
            "/api/meals/0", "/api/meals?size=0", "/api/meals/-1"})
    @DisplayName("날짜, 페이지, ID가 잘못되면 400을 반환한다")
    void invalidQueryOrPathReturns400(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        verifyNoInteractions(mealService);
    }

    @Test
    @DisplayName("잘못된 JSON은 400을 반환한다")
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    @DisplayName("없는 급식은 404를 반환한다")
    void missingMealReturns404() throws Exception {
        given(mealService.getMeal(999L)).willThrow(new BusinessException(ErrorCode.MEAL_NOT_FOUND));
        mvc.perform(get("/api/meals/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("MEAL_NOT_FOUND"));
    }

    @Test
    @DisplayName("중복 급식은 409를 반환한다")
    void duplicateMealReturns409() throws Exception {
        given(mealService.createMeal(any())).willThrow(new BusinessException(ErrorCode.DUPLICATE_MEAL));
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-10","mealType":"LUNCH","menu":"쌀밥"}
                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_MEAL"));
    }

    @Test
    @DisplayName("지원하지 않는 메서드는 405와 Allow 헤더를 반환한다")
    void unsupportedMethodKeeps405AndAllowHeader() throws Exception {
        mvc.perform(patch("/api/meals/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("지원하지 않는 Content-Type은 415를 반환한다")
    void unsupportedContentTypeReturns415() throws Exception {
        mvc.perform(post("/api/meals").contentType(MediaType.TEXT_PLAIN).content("menu"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    @DisplayName("급식 수정에는 날짜, 구분, 메뉴가 모두 필요하다")
    void putRequiresAllFields() throws Exception {
        mvc.perform(put("/api/meals/1").contentType(MediaType.APPLICATION_JSON).content("{\"menu\":\"메뉴\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealService);
    }
}
