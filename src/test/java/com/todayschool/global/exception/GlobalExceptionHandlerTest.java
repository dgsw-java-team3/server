package com.todayschool.global.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.todayschool.global.response.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 성공·실패 응답이 모두 같은 JSON 형식인지 확인합니다. */
@WebMvcTest(GlobalExceptionHandlerTest.TestController.class)
@Import(GlobalExceptionHandlerTest.TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("성공 응답은 success, data, message 형식입니다")
    void successResponse() throws Exception {
        mockMvc.perform(get("/test/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("item-1"))
                .andExpect(jsonPath("$.message").value("요청이 성공했습니다."));
    }

    @Test
    @DisplayName("삭제 응답은 204 대신 200과 data: null을 반환합니다")
    void deleteResponseKeepsNullData() throws Exception {
        mockMvc.perform(delete("/test/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").hasJsonPath())
                .andExpect(jsonPath("$.data").value(nullValue()))
                .andExpect(jsonPath("$.message").value("삭제되었습니다."));
    }

    @Test
    @DisplayName("BusinessException은 ErrorCode의 상태코드와 code로 변환됩니다")
    void businessException() throws Exception {
        mockMvc.perform(get("/test/items/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.error.message").value("요청한 경로를 찾을 수 없습니다."));
    }

    @Test
    @DisplayName("@Valid 검증 실패는 400 INVALID_INPUT_VALUE와 필드명을 반환합니다")
    void bodyValidationFailure() throws Exception {
        mockMvc.perform(post("/test/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.error.message", containsString("name")));
    }

    @Test
    @DisplayName("존재하지 않는 enum 쿼리 값은 400 INVALID_INPUT_VALUE가 됩니다")
    void enumTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/items").param("color", "PURPLE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.error.message", containsString("color")));
    }

    @Test
    @DisplayName("깨진 JSON 본문은 400 INVALID_INPUT_VALUE가 됩니다")
    void malformedJson() throws Exception {
        mockMvc.perform(post("/test/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"));
    }

    @Test
    @DisplayName("예상하지 못한 예외는 500이 되고 내부 메시지를 노출하지 않습니다")
    void unexpectedException() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.error.message").value("서버 내부 오류가 발생했습니다."));
    }

    enum Color { RED, BLUE }

    record CreateRequest(@NotBlank String name) {
    }

    @RestController
    static class TestController {

        @GetMapping("/test/items/{id}")
        ApiResponse<String> getItem(@PathVariable Long id) {
            if (id == 999L) {
                throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND);
            }
            return ApiResponse.ok("item-" + id);
        }

        @GetMapping("/test/items")
        ApiResponse<String> search(@RequestParam Color color) {
            return ApiResponse.ok(color.name());
        }

        @PostMapping("/test/items")
        ApiResponse<String> create(@Valid @RequestBody CreateRequest request) {
            return ApiResponse.ok(request.name());
        }

        @DeleteMapping("/test/items/{id}")
        ApiResponse<Void> deleteItem(@PathVariable Long id) {
            return ApiResponse.ok(null, "삭제되었습니다.");
        }

        @GetMapping("/test/boom")
        ApiResponse<Void> boom() {
            throw new IllegalStateException("internal detail that must not leak");
        }
    }
}
