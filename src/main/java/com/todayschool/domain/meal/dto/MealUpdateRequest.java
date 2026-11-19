package com.todayschool.domain.meal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MealUpdateRequest(
        @NotBlank @Size(max = 1000) @Schema(example = "쌀밥, 된장국, 불고기, 김치") String menu) {}
