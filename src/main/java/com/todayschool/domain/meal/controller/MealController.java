package com.todayschool.domain.meal.controller;

import com.todayschool.global.response.ApiResponse;
import com.todayschool.domain.meal.dto.*;
import com.todayschool.domain.meal.service.MealService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.time.LocalDate;

@Tag(name = "급식", description = "급식 등록·목록·상세·수정·삭제")
@RestController
@RequestMapping("/api/meals")
@RequiredArgsConstructor
public class MealController {
    private final MealService mealService;

    @Operation(summary = "급식 등록", description = "날짜와 구분의 조합은 유일해야 합니다. 중복은 409입니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<MealResponse>> createMeal(@Valid @RequestBody MealCreateRequest request) {
        var meal = mealService.createMeal(request);
        return ResponseEntity.created(URI.create("/api/meals/" + meal.id())).body(ApiResponse.ok(meal));
    }

    @Operation(summary = "급식 목록 조회", description = "날짜 생략 시 전체 조회. page는 0부터, size는 1~100입니다.")
    @GetMapping
    public ApiResponse<MealPageResponse> getMeals(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ApiResponse.ok(mealService.getMeals(date, page, size));
    }

    @Operation(summary = "급식 상세 조회")
    @GetMapping("/{mealId}")
    public ApiResponse<MealResponse> getMeal(@PathVariable @Positive Long mealId) {
        return ApiResponse.ok(mealService.getMeal(mealId));
    }

    @Operation(summary = "급식 수정", description = "date, mealType, menu를 모두 보내는 PUT 요청입니다.")
    @PutMapping("/{mealId}")
    public ApiResponse<MealResponse> updateMeal(@PathVariable @Positive Long mealId,
                                               @Valid @RequestBody MealUpdateRequest request) {
        return ApiResponse.ok(mealService.updateMeal(mealId, request));
    }

    @Operation(summary = "급식 삭제")
    @DeleteMapping("/{mealId}")
    public ApiResponse<Void> deleteMeal(@PathVariable @Positive Long mealId) {
        mealService.deleteMeal(mealId);
        return ApiResponse.ok(null, "급식이 삭제되었습니다.");
    }
}
