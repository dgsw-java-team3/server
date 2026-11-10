package com.todayschool.domain.meal.dto;

import com.todayschool.domain.meal.entity.MealType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record MealUpdateRequest(
        @NotNull @Schema(example = "2026-11-10") LocalDate date,
        @NotNull @Schema(example = "LUNCH") MealType mealType,
        @NotBlank @Size(max = 1000) @Schema(example = "쌀밥, 된장국, 불고기, 김치") String menu) {}
