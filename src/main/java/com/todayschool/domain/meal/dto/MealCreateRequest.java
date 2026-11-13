package com.todayschool.domain.meal.dto;

import com.todayschool.domain.meal.entity.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MealCreateRequest(
        @NotNull @Schema(example = "2026-11-10") LocalDate date,
        @NotNull @Schema(example = "LUNCH") MealType mealType,
        @NotBlank @Size(max = 1000) @Schema(example = "쌀밥, 미역국, 제육볶음, 김치") String menu) {}
