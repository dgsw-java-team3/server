package com.todayschool.domain.meal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewCreateRequest(
        @NotNull @Min(1) @Max(5) @Schema(example = "5") Integer rating,
        @NotBlank @Size(max = 200) @Schema(example = "맛있어요") String comment,
        @NotBlank @Size(max = 30) @Schema(example = "학생1") String nickname) {}
