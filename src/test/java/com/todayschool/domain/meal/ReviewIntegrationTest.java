package com.todayschool.domain.meal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql("/sql/meal-review.sql")
class ReviewIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    @DisplayName("리뷰를 작성하면 201을 반환하고 급식 상세의 별점과 리뷰 수에 반영된다")
    void createReviewUpdatesMealSummary() throws Exception {
        mvc.perform(post("/api/meals/102/reviews").contentType(MediaType.APPLICATION_JSON).content("""
                {"rating":1,"comment":" 아쉬워요 ","nickname":" 학생7 "}
                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.mealId").value(102))
                .andExpect(jsonPath("$.data.rating").value(1))
                .andExpect(jsonPath("$.data.comment").value("아쉬워요"))
                .andExpect(jsonPath("$.data.nickname").value("학생7"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty());
        mvc.perform(get("/api/meals/102")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRating").value(3.0))
                .andExpect(jsonPath("$.data.reviewCount").value(3));
    }

    @Test
    @DisplayName("리뷰는 최신순이며 같은 시각에는 ID가 큰 리뷰부터 반환한다")
    void reviewsAreNewestFirstAndDoNotIncludeOtherMeals() throws Exception {
        jdbc.update("""
                INSERT INTO meal_reviews (id, meal_id, rating, comment, nickname, created_at, updated_at)
                VALUES (207, 102, 4, '같은 시각', '학생7', '2030-01-10 11:00:00', '2030-01-10 11:00:00')
                """);
        mvc.perform(get("/api/meals/102/reviews")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", contains(207, 203, 202)));
    }

    @Test
    @DisplayName("리뷰가 없는 급식은 빈 목록, 평균 0, 리뷰 수 0을 반환한다")
    void mealWithoutReviewsHasEmptyListAndZeroSummary() throws Exception {
        mvc.perform(get("/api/meals/105/reviews")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        mvc.perform(get("/api/meals/105")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.averageRating").value(0.0))
                .andExpect(jsonPath("$.data.reviewCount").value(0));
    }

    @Test
    @DisplayName("리뷰 삭제 후 상세 집계와 순위가 바뀌며 마지막 리뷰 삭제 시 순위에서 빠진다")
    void deletingReviewsRecalculatesSummaryAndRanking() throws Exception {
        mvc.perform(delete("/api/reviews/203")).andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty());
        mvc.perform(get("/api/meals/102")).andExpect(jsonPath("$.data.averageRating").value(5.0))
                .andExpect(jsonPath("$.data.reviewCount").value(1));
        mvc.perform(get("/api/meals/ranking?startDate=2030-01-02&endDate=2030-01-04"))
                .andExpect(jsonPath("$.data[*].mealId", contains(102, 104, 103)));
        mvc.perform(delete("/api/reviews/202")).andExpect(status().isOk());
        mvc.perform(get("/api/meals/102")).andExpect(jsonPath("$.data.averageRating").value(0.0))
                .andExpect(jsonPath("$.data.reviewCount").value(0));
        mvc.perform(get("/api/meals/ranking?startDate=2030-01-02&endDate=2030-01-04"))
                .andExpect(jsonPath("$.data[*].mealId", contains(104, 103)));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM meal_reviews WHERE meal_id = 102", Long.class))
                .isZero();
    }

    @Test
    @DisplayName("급식 삭제는 연결된 리뷰만 함께 삭제한다")
    void deletingMealCascadesToItsReviews() throws Exception {
        mvc.perform(delete("/api/meals/102")).andExpect(status().isOk());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM meal_reviews WHERE meal_id = 102", Long.class))
                .isZero();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM meal_reviews", Long.class)).isEqualTo(4);
        mvc.perform(get("/api/meals/102/reviews")).andExpect(status().isNotFound());
        mvc.perform(delete("/api/reviews/202")).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("순위는 급식 날짜 양끝을 포함하고 기간 밖과 리뷰 없는 급식을 제외한다")
    void rankingUsesMealDatesAndAverageThenCount() throws Exception {
        mvc.perform(get("/api/meals/ranking?startDate=2030-01-02&endDate=2030-01-04"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].mealId", contains(104, 102, 103)))
                .andExpect(jsonPath("$.data[1].averageRating").value(4.0))
                .andExpect(jsonPath("$.data[1].reviewCount").value(2));
        mvc.perform(get("/api/meals/ranking?startDate=2030-01-02&endDate=2030-01-02"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].mealId", contains(102)));
        mvc.perform(get("/api/meals/ranking?startDate=2040-01-01&endDate=2040-01-31"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"rating\":0,\"comment\":\"맛있음\",\"nickname\":\"학생\"}",
            "{\"rating\":6,\"comment\":\"맛있음\",\"nickname\":\"학생\"}",
            "{\"comment\":\"맛있음\",\"nickname\":\"학생\"}",
            "{\"rating\":5,\"comment\":\" \",\"nickname\":\"학생\"}",
            "{\"rating\":5,\"comment\":\"맛있음\",\"nickname\":\" \"}"})
    @DisplayName("별점 범위, 필수 값, 공백을 검증하고 잘못된 리뷰는 저장하지 않는다")
    void invalidReviewIsRejected(String body) throws Exception {
        mvc.perform(post("/api/meals/102/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM meal_reviews", Long.class)).isEqualTo(6);
    }

    @Test
    @DisplayName("한줄평과 닉네임의 최대 길이를 검증한다")
    void oversizedReviewIsRejected() throws Exception {
        for (String body : new String[] {
                "{\"rating\":5,\"comment\":\"" + "가".repeat(201) + "\",\"nickname\":\"학생\"}",
                "{\"rating\":5,\"comment\":\"맛있음\",\"nickname\":\"" + "가".repeat(31) + "\"}"}) {
            mvc.perform(post("/api/meals/102/reviews").contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/meals/ranking", "/api/meals/ranking?startDate=2030-01-01",
            "/api/meals/ranking?startDate=wrong&endDate=2030-01-03",
            "/api/meals/ranking?startDate=2030-01-04&endDate=2030-01-02",
            "/api/meals/0/reviews"})
    @DisplayName("잘못된 기간과 급식 ID는 400을 반환한다")
    void invalidQueryOrPathIs400(String path) throws Exception {
        mvc.perform(get(path)).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    @DisplayName("없는 급식에 리뷰를 작성하거나 조회하면 404이며 없는 리뷰 삭제도 404이다")
    void missingResourcesAre404() throws Exception {
        mvc.perform(post("/api/meals/999/reviews").contentType(MediaType.APPLICATION_JSON).content("""
                {"rating":5,"comment":"맛있음","nickname":"학생"}
                """))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("MEAL_NOT_FOUND"));
        mvc.perform(get("/api/meals/999/reviews")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("MEAL_NOT_FOUND"));
        mvc.perform(delete("/api/reviews/999")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error.code").value("REVIEW_NOT_FOUND"));
    }

    @Test
    @DisplayName("메뉴만 수정해도 날짜, 끼니, 리뷰 집계가 유지된다")
    void menuOnlyUpdateKeepsMealIdentityAndReviews() throws Exception {
        mvc.perform(put("/api/meals/102").contentType(MediaType.APPLICATION_JSON).content("{\"menu\":\"새 메뉴\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/meals/102")).andExpect(jsonPath("$.data.menu").value("새 메뉴"))
                .andExpect(jsonPath("$.data.date").value("2030-01-02"))
                .andExpect(jsonPath("$.data.mealType").value("LUNCH"))
                .andExpect(jsonPath("$.data.reviewCount").value(2));
    }

    @Test
    @DisplayName("DB에서도 1~5 범위를 벗어난 별점을 막는다")
    void databaseRejectsInvalidRating() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO meal_reviews (meal_id, rating, comment, nickname, created_at, updated_at)
                VALUES (102, 6, '잘못된 별점', '학생', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                """)).isInstanceOf(DataIntegrityViolationException.class);
    }
}
