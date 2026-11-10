package com.todayschool.domain.meal;

import com.todayschool.global.exception.*;
import com.todayschool.domain.meal.controller.MealController;
import com.todayschool.domain.meal.dto.MealResponse;
import com.todayschool.domain.meal.entity.MealType;
import com.todayschool.domain.meal.service.MealService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.time.LocalDate;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MealController.class)
@Import(GlobalExceptionHandler.class)
class MealControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean MealService mealService;

    @Test
    void createMealReturns201AndLocation() throws Exception {
        given(mealService.createMeal(any())).willReturn(new MealResponse(1L, LocalDate.of(2026, 11, 10),
                MealType.LUNCH, "쌀밥, 불고기", LocalDateTime.now(), LocalDateTime.now()));
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-10","mealType":"LUNCH","menu":"쌀밥, 불고기"}
                """))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/meals/1"))
                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void blankMenuReturns400WithFieldDetails() throws Exception {
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-10","mealType":"LUNCH","menu":" "}
                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.error.message", org.hamcrest.Matchers.containsString("menu")));
        verifyNoInteractions(mealService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/meals?date=wrong", "/api/meals?size=101", "/api/meals?page=-1",
            "/api/meals/0", "/api/meals?size=0", "/api/meals/-1"})
    void invalidQueryOrPathReturns400(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        verifyNoInteractions(mealService);
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("{broken"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    void missingMealReturns404() throws Exception {
        given(mealService.getMeal(999L)).willThrow(new BusinessException(ErrorCode.MEAL_NOT_FOUND));
        mvc.perform(get("/api/meals/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("MEAL_NOT_FOUND"));
    }

    @Test
    void duplicateMealReturns409() throws Exception {
        given(mealService.createMeal(any())).willThrow(new BusinessException(ErrorCode.DUPLICATE_MEAL));
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-10","mealType":"LUNCH","menu":"쌀밥"}
                """))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error.code").value("DUPLICATE_MEAL"));
    }

    @Test
    void unsupportedMethodKeeps405AndAllowHeader() throws Exception {
        mvc.perform(patch("/api/meals/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed()).andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void unsupportedContentTypeReturns415() throws Exception {
        mvc.perform(post("/api/meals").contentType(MediaType.TEXT_PLAIN).content("menu"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.error.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    void putRequiresAllFields() throws Exception {
        mvc.perform(put("/api/meals/1").contentType(MediaType.APPLICATION_JSON).content("{\"menu\":\"메뉴\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealService);
    }
}
