package com.todayschool.lostitem.dto;

import com.todayschool.lostitem.domain.ItemCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record LostItemCreateRequest(
        @NotBlank @Size(max = 100) @Schema(example = "검은색 반지갑") String title,
        @Size(max = 500) @Schema(example = "학생증과 교통카드가 들어 있음") String description,
        @NotNull @Schema(example = "WALLET") ItemCategory category,
        @NotBlank @Size(max = 100) @Schema(example = "본관 2층 복도") String foundPlace,
        @NotNull @PastOrPresent @Schema(example = "2026-11-10") LocalDate foundDate) {}
