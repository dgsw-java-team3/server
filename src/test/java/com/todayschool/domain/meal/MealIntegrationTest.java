package com.todayschool.domain.meal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = "DELETE FROM meals")
@Sql(scripts = "/data.sql")
class MealIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    @Test
    void createReadUpdateDeleteWithSeedData() throws Exception {
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-12","mealType":"LUNCH","menu":"볶음밥"}
                """))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/meals/6"))
                .andExpect(jsonPath("$.data.id").value(6))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        mvc.perform(get("/api/meals/6")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.menu").value("볶음밥"));
        mvc.perform(put("/api/meals/6").contentType(MediaType.APPLICATION_JSON).content("""
                {"date":"2026-11-12","mealType":"LUNCH","menu":"김치볶음밥"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.menu").value("김치볶음밥"));
        mvc.perform(get("/api/meals/6")).andExpect(jsonPath("$.data.menu").value("김치볶음밥"));
        mvc.perform(delete("/api/meals/6")).andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.message").value("급식이 삭제되었습니다."));
        mvc.perform(get("/api/meals/6")).andExpect(status().isNotFound());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM meals", Long.class)).isEqualTo(5);
    }

    @Test
    void dateFilterAndPaginationUseQueryMethod() throws Exception {
        mvc.perform(get("/api/meals?date=2026-11-10&page=0&size=1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.totalElements").value(2))
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
    void duplicateCreateAndUpdateAre409AndKeepDatabaseUnchanged() throws Exception {
        String body = """
                {"date":"2026-11-10","mealType":"LUNCH","menu":"중복"}
                """;
        mvc.perform(post("/api/meals").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error.code").value("DUPLICATE_MEAL"));
        mvc.perform(put("/api/meals/3").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error.code").value("DUPLICATE_MEAL"));
        mvc.perform(get("/api/meals/3")).andExpect(jsonPath("$.data.mealType").value("DINNER"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM meals", Long.class)).isEqualTo(5);
    }

    @Test
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
    void databaseUniqueConstraintRejectsBypassedValidation() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO meals (meal_date, meal_type, menu, created_at, updated_at)
                VALUES ('2026-11-10', 'LUNCH', '중복', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)) .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }

    @Test
    void openApiDocumentsExactlyFiveMealOperations() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andExpect(jsonPath("$.paths.length()").value(2))
                .andExpect(jsonPath("$.paths['/api/meals'].get").exists())
                .andExpect(jsonPath("$.paths['/api/meals'].post").exists())
                .andExpect(jsonPath("$.paths['/api/meals/{mealId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/meals/{mealId}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/meals/{mealId}'].delete").exists());
    }
}
