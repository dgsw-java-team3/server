package com.todayschool.domain.meal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = "DELETE FROM meals")
@Sql(scripts = "/data.sql")
class MealIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("초기 데이터가 있는 상태에서 급식 CRUD가 동작한다")
    void createReadUpdateDeleteWithSeedData() throws Exception {
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-12","mealType":"LUNCH","menu":"볶음밥"}
                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/meals/6"))
                .andExpect(jsonPath("$.data.id").value(6))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        mvc.perform(get("/api/meals/6")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menu").value("볶음밥"));
        mvc.perform(put("/api/meals/6").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-12","mealType":"LUNCH","menu":"김치볶음밥"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menu").value("김치볶음밥"));
        mvc.perform(get("/api/meals/6")).andExpect(jsonPath("$.data.menu").value("김치볶음밥"));
        mvc.perform(delete("/api/meals/6")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.message").value("급식이 삭제되었습니다."));
        mvc.perform(get("/api/meals/6")).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM meals", Long.class)).isEqualTo(5);
    }

    @Test
    @DisplayName("날짜별 조회와 페이징이 적용된다")
    void dateFilterAndPaginationUseQueryMethod() throws Exception {
        mvc.perform(get("/api/meals?date=2026-11-10&page=0&size=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(2))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(2));
        mvc.perform(get("/api/meals?date=2026-11-10&page=1&size=1"))
                .andExpect(jsonPath("$.data.content[0].id").value(3));
        mvc.perform(get("/api/meals?date=2026-12-01"))
                .andExpect(jsonPath("$.data.content").isEmpty());
        mvc.perform(get("/api/meals"))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.content[0].id").value(4));
    }

    @Test
    @DisplayName("중복 등록과 수정은 기존 데이터를 유지한다")
    void duplicateCreateAndUpdateAre409AndKeepDatabaseUnchanged() throws Exception {
        String body = """
                {"date":"2026-11-10","mealType":"LUNCH","menu":"중복"}
                """;
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_MEAL"));
        mvc.perform(put("/api/meals/3").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("DUPLICATE_MEAL"));
        mvc.perform(get("/api/meals/3")).andExpect(jsonPath("$.data.mealType").value("DINNER"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM meals", Long.class)).isEqualTo(5);
    }

    @Test
    @DisplayName("없는 급식의 조회, 수정, 삭제는 404를 반환한다")
    void missingMealIs404ForReadUpdateAndDelete() throws Exception {
        mvc.perform(get("/api/meals/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("MEAL_NOT_FOUND"));
        mvc.perform(put("/api/meals/999").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-12","mealType":"LUNCH","menu":"메뉴"}
                """))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/meals/999")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("애플리케이션에서 잘못된 입력을 거부한다")
    void invalidQueryRemains400WithRealApplication() throws Exception {
        mvc.perform(get("/api/meals?size=101")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        mvc.perform(get("/api/meals/0")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-10","mealType":"SNACK","menu":"메뉴"}
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DB에서 같은 날짜와 구분의 급식 중복을 막는다")
    void databaseUniqueConstraintRejectsBypassedValidation() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO meals (meal_date, meal_type, menu, created_at, updated_at)
                VALUES ('2026-11-10', 'LUNCH', '중복', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("급식 API 5개가 OpenAPI에 포함된다")
    void openApiDocumentsMealCrud() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/meals'].get").exists())
                .andExpect(jsonPath("$.paths['/api/meals'].post").exists())
                .andExpect(jsonPath("$.paths['/api/meals/{mealId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/meals/{mealId}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/meals/{mealId}'].delete").exists());
    }
}
